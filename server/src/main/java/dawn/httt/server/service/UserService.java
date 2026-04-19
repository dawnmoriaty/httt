package dawn.httt.server.service;

import dawn.httt.server.constant.CommonStatusConstant;
import dawn.httt.server.dto.request.CreateUserRequest;
import dawn.httt.server.dto.request.UpdateUserRolesRequest;
import dawn.httt.server.dto.response.RoleSummaryResponse;
import dawn.httt.server.dto.response.UserResponse;
import dawn.httt.server.entity.RoleEntity;
import dawn.httt.server.entity.UserEntity;
import dawn.httt.server.exception.BadRequestException;
import dawn.httt.server.exception.NotFoundException;
import dawn.httt.server.repository.RoleRepository;
import dawn.httt.server.repository.UserRepository;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthSessionService authSessionService;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthSessionService authSessionService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authSessionService = authSessionService;
    }

    public Page<UserResponse> getAllUsers(String query, Pageable pageable) {
        Page<UserEntity> page = hasText(query)
                ? userRepository.searchByKeyword(query.trim(), pageable)
                : userRepository.findAllByOrderByIdAsc(pageable);

        List<Long> ids = page.getContent().stream().map(UserEntity::getId).toList();
        Map<Long, UserEntity> userMap = userRepository.findWithRolesByIdIn(ids)
                .stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));

        return page.map(userEntity -> toResponse(userMap.getOrDefault(userEntity.getId(), userEntity)));
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        validateUniqueUser(request.getUsername(), request.getEmail(), null);
        List<RoleEntity> roleEntities = roleRepository.findAllById(request.getRoleIds());
        if (roleEntities.size() != request.getRoleIds().size()) {
            throw new BadRequestException("ROLE_NOT_FOUND", "Co role khong ton tai.");
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(request.getUsername().trim());
        userEntity.setFullName(request.getFullName().trim());
        userEntity.setEmail(request.getEmail().trim().toLowerCase());
        userEntity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userEntity.setStatus(CommonStatusConstant.STATUS_ACTIVE);
        userEntity.setSessionVersion(1L);
        userEntity.setRoles(new LinkedHashSet<>(roleEntities));
        return toResponse(userRepository.save(userEntity));
    }

    @Transactional
    public UserResponse updateUserRoles(Long userId, UpdateUserRolesRequest request) {
        UserEntity userEntity = userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "Khong tim thay user."));
        List<RoleEntity> roleEntities = roleRepository.findAllById(request.getRoleIds());
        if (roleEntities.size() != request.getRoleIds().size()) {
            throw new BadRequestException("ROLE_NOT_FOUND", "Co role khong ton tai.");
        }

        userEntity.setRoles(new LinkedHashSet<>(roleEntities));
        bumpSessionVersion(userEntity.getId());
        UserEntity savedUser = userRepository.save(userEntity);
        authSessionService.invalidateUserSessions(userEntity.getId());
        return toResponse(savedUser);
    }

    @Transactional
    public void bumpSessionVersion(Long userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "Khong tim thay user."));
        userEntity.setSessionVersion(userEntity.getSessionVersion() + 1);
        userRepository.save(userEntity);
    }

    public UserEntity getUserWithRoles(Long userId) {
        return userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "Khong tim thay user."));
    }

    public UserEntity getUserWithRolesByUsername(String username) {
        return userRepository.findWithRolesByUsername(username)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "Khong tim thay user."));
    }

    private void validateUniqueUser(String username, String email, Long ignoreUserId) {
        if (ignoreUserId == null && userRepository.existsByUsername(username.trim())) {
            throw new BadRequestException("USERNAME_EXISTS", "Username da ton tai.");
        }
        if (ignoreUserId == null && userRepository.existsByEmail(email.trim().toLowerCase())) {
            throw new BadRequestException("EMAIL_EXISTS", "Email da ton tai.");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private UserResponse toResponse(UserEntity userEntity) {
        return UserResponse.builder()
                .id(userEntity.getId())
                .username(userEntity.getUsername())
                .fullName(userEntity.getFullName())
                .email(userEntity.getEmail())
                .status(userEntity.getStatus())
                .sessionVersion(userEntity.getSessionVersion())
                .roles(userEntity.getRoles().stream()
                        .map(roleEntity -> new RoleSummaryResponse(roleEntity.getId(), roleEntity.getCode(), roleEntity.getName()))
                        .toList())
                .build();
    }
}
