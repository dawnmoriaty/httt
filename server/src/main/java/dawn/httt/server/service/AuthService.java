package dawn.httt.server.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import dawn.httt.server.config.AppSecurityProperties;
import dawn.httt.server.constant.CommonStatusConstant;
import dawn.httt.server.constant.RoleCodeConstant;
import dawn.httt.server.dto.request.LoginRequest;
import dawn.httt.server.dto.request.RefreshTokenRequest;
import dawn.httt.server.dto.request.RegisterRequest;
import dawn.httt.server.dto.response.AuthResponse;
import dawn.httt.server.dto.response.CurrentUserResponse;
import dawn.httt.server.entity.PermissionEntity;
import dawn.httt.server.entity.RoleEntity;
import dawn.httt.server.entity.UserEntity;
import dawn.httt.server.exception.BadRequestException;
import dawn.httt.server.exception.NotFoundException;
import dawn.httt.server.exception.UnauthorizedException;
import dawn.httt.server.repository.RoleRepository;
import dawn.httt.server.repository.UserRepository;
import dawn.httt.server.security.AuthenticatedUser;
import dawn.httt.server.security.CurrentAuthenticatedUserProvider;
import dawn.httt.server.security.JwtTokenProvider;
import dawn.httt.server.security.RefreshSession;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthSessionService authSessionService;
    private final AppSecurityProperties appSecurityProperties;
    private final PermissionGuard permissionGuard;
    private final CurrentAuthenticatedUserProvider currentAuthenticatedUserProvider;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            AuthSessionService authSessionService,
            AppSecurityProperties appSecurityProperties,
            PermissionGuard permissionGuard,
            CurrentAuthenticatedUserProvider currentAuthenticatedUserProvider
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authSessionService = authSessionService;
        this.appSecurityProperties = appSecurityProperties;
        this.permissionGuard = permissionGuard;
        this.currentAuthenticatedUserProvider = currentAuthenticatedUserProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername().trim())) {
            throw new BadRequestException("USERNAME_EXISTS", "Username da ton tai.");
        }
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BadRequestException("EMAIL_EXISTS", "Email da ton tai.");
        }

        RoleEntity defaultRole = roleRepository.findByCode(RoleCodeConstant.USER)
                .orElseThrow(() -> new NotFoundException("ROLE_NOT_FOUND", "Khong tim thay role mac dinh USER."));

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(request.getUsername().trim());
        userEntity.setFullName(request.getFullName().trim());
        userEntity.setEmail(request.getEmail().trim().toLowerCase());
        userEntity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userEntity.setStatus(CommonStatusConstant.STATUS_ACTIVE);
        userEntity.getRoles().add(defaultRole);

        UserEntity savedUser = userRepository.save(userEntity);
        return issueAuthResponse(userRepository.findWithRolesById(savedUser.getId())
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "Khong tim thay user vua tao.")), defaultRole);
    }

    public AuthResponse login(LoginRequest request) {
        UserEntity userEntity = userRepository.findAuthSnapshotByUsername(request.getUsername().trim())
                .orElseThrow(() -> new UnauthorizedException("INVALID_CREDENTIALS", "Thong tin dang nhap khong hop le."));

        if (!passwordEncoder.matches(request.getPassword(), userEntity.getPasswordHash())) {
            throw new UnauthorizedException("INVALID_CREDENTIALS", "Thong tin dang nhap khong hop le.");
        }
        if (userEntity.getStatus() == null || userEntity.getStatus() != CommonStatusConstant.STATUS_ACTIVE) {
            throw new UnauthorizedException("USER_INACTIVE", "Tai khoan dang khong hoat dong.");
        }

        RoleEntity selectedRole = resolveSelectedRole(userEntity, request.getSelectedRoleCode());
        return issueAuthResponse(userEntity, selectedRole);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshSession refreshSession = authSessionService.getRefreshSession(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("INVALID_REFRESH_TOKEN", "Refresh token khong hop le."));

        UserEntity userEntity = userRepository.findAuthSnapshotById(refreshSession.getUserId())
                .orElseThrow(() -> new UnauthorizedException("INVALID_REFRESH_TOKEN", "Refresh token khong hop le."));

        if (userEntity.getStatus() == null || userEntity.getStatus() != CommonStatusConstant.STATUS_ACTIVE) {
            authSessionService.invalidateUserSessions(userEntity.getId());
            throw new UnauthorizedException("USER_INACTIVE", "Tai khoan dang khong hoat dong.");
        }

        RoleEntity selectedRole = userEntity.getRoles().stream()
                .filter(roleEntity -> roleEntity.getId().equals(refreshSession.getSelectedRoleId()))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("ROLE_NOT_FOUND", "Role dang nhap khong con ton tai."));

        if (selectedRole.getStatus() == null || selectedRole.getStatus() != CommonStatusConstant.STATUS_ACTIVE) {
            authSessionService.invalidateUserSessions(userEntity.getId());
            throw new UnauthorizedException("ROLE_INACTIVE", "Role dang nhap khong con hoat dong.");
        }

        authSessionService.removeRefreshSession(userEntity.getId(), request.getRefreshToken());
        return issueAuthResponse(userEntity, selectedRole);
    }

    public void logout(String bearerToken) {
        String token = bearerToken == null ? "" : bearerToken.replace("Bearer ", "").trim();
        if (!StringUtils.hasText(token)) {
            throw new UnauthorizedException("MISSING_TOKEN", "Ban chua gui access token.");
        }

        DecodedJWT decodedJWT = jwtTokenProvider.verifyAccessToken(token);
        Long userId = decodedJWT.getClaim("userId").asLong();
        String tokenId = decodedJWT.getId();
        Duration remainingDuration = Duration.between(Instant.now(), decodedJWT.getExpiresAtAsInstant());

        authSessionService.blacklist(tokenId, remainingDuration);
        authSessionService.invalidateUserSessions(userId);
    }

    public CurrentUserResponse currentUser() {
        AuthenticatedUser authenticatedUser = currentAuthenticatedUserProvider.getCurrentUser();
        return toCurrentUserResponse(authenticatedUser);
    }

    private AuthResponse issueAuthResponse(UserEntity userEntity, RoleEntity selectedRole) {
        String accessTokenId = UUID.randomUUID().toString();
        String refreshToken = UUID.randomUUID().toString();
        Duration accessDuration = appSecurityProperties.getAccessTokenDuration();
        Duration refreshDuration = appSecurityProperties.getRefreshTokenDuration();
        Instant accessExpiresAt = Instant.now().plus(accessDuration);

        AuthenticatedUser authenticatedUser = buildAuthenticatedUser(userEntity, selectedRole);
        String accessToken = jwtTokenProvider.generateAccessToken(authenticatedUser, accessTokenId, accessExpiresAt);

        authSessionService.saveAccessSession(accessTokenId, authenticatedUser, accessDuration);
        authSessionService.saveRefreshSession(refreshToken, RefreshSession.builder()
                .userId(authenticatedUser.getUserId())
                .selectedRoleId(authenticatedUser.getSelectedRoleId())
                .sessionVersion(authenticatedUser.getSessionVersion())
                .build(), refreshDuration);

        return new AuthResponse(
                accessToken,
                refreshToken,
                accessDuration.toSeconds(),
                toCurrentUserResponse(authenticatedUser)
        );
    }

    private AuthenticatedUser buildAuthenticatedUser(UserEntity userEntity, RoleEntity selectedRole) {
        List<String> permissions = selectedRole.getPermissions().stream()
                .filter(permissionEntity -> permissionEntity.getStatus() != null
                        && permissionEntity.getStatus() == CommonStatusConstant.STATUS_ACTIVE)
                .map(permissionEntity -> permissionGuard.toPermissionKey(permissionEntity.getResourceCode(), permissionEntity.getActionCode()))
                .sorted()
                .toList();

        List<String> roleCodes = userEntity.getRoles().stream()
                .filter(roleEntity -> roleEntity.getStatus() != null
                        && roleEntity.getStatus() == CommonStatusConstant.STATUS_ACTIVE)
                .map(RoleEntity::getCode)
                .sorted()
                .toList();

        return AuthenticatedUser.builder()
                .userId(userEntity.getId())
                .username(userEntity.getUsername())
                .fullName(userEntity.getFullName())
                .email(userEntity.getEmail())
                .selectedRoleId(selectedRole.getId())
                .selectedRoleCode(selectedRole.getCode())
                .roleCodes(roleCodes)
                .permissions(permissions)
                .sessionVersion(userEntity.getSessionVersion())
                .build();
    }

    private RoleEntity resolveSelectedRole(UserEntity userEntity, String selectedRoleCode) {
        if (userEntity.getRoles().isEmpty()) {
            throw new UnauthorizedException("NO_ROLE_ASSIGNED", "Tai khoan chua duoc gan role.");
        }

        if (!StringUtils.hasText(selectedRoleCode)) {
            return userEntity.getRoles().stream()
                    .filter(roleEntity -> roleEntity.getStatus() != null
                            && roleEntity.getStatus() == CommonStatusConstant.STATUS_ACTIVE)
                    .min(Comparator.comparing(RoleEntity::getId))
                    .orElseThrow(() -> new UnauthorizedException("ROLE_INACTIVE", "Khong co role hoat dong de dang nhap."));
        }

        return userEntity.getRoles().stream()
                .filter(roleEntity -> roleEntity.getCode().equalsIgnoreCase(selectedRoleCode.trim()))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("ROLE_NOT_ASSIGNED", "Role duoc chon khong thuoc tai khoan nay."));
    }

    private CurrentUserResponse toCurrentUserResponse(AuthenticatedUser authenticatedUser) {
        return CurrentUserResponse.builder()
                .id(authenticatedUser.getUserId())
                .username(authenticatedUser.getUsername())
                .fullName(authenticatedUser.getFullName())
                .email(authenticatedUser.getEmail())
                .selectedRoleId(authenticatedUser.getSelectedRoleId())
                .selectedRoleCode(authenticatedUser.getSelectedRoleCode())
                .roleCodes(authenticatedUser.getRoleCodes())
                .permissions(authenticatedUser.getPermissions())
                .build();
    }
}
