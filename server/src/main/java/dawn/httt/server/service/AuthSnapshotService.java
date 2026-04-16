package dawn.httt.server.service;

import dawn.httt.server.constant.CommonStatusConstant;
import dawn.httt.server.entity.RoleEntity;
import dawn.httt.server.entity.UserEntity;
import dawn.httt.server.exception.UnauthorizedException;
import dawn.httt.server.repository.UserRepository;
import dawn.httt.server.security.AuthenticatedUser;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthSnapshotService {

    private final UserRepository userRepository;
    private final PermissionGuard permissionGuard;

    public AuthSnapshotService(UserRepository userRepository, PermissionGuard permissionGuard) {
        this.userRepository = userRepository;
        this.permissionGuard = permissionGuard;
    }

    @Transactional(readOnly = true)
    public AuthenticatedUser resolveFreshAuthenticatedUser(Long userId, Long selectedRoleId) {
        UserEntity userEntity = userRepository.findAuthSnapshotById(userId)
                .orElseThrow(() -> new UnauthorizedException("USER_NOT_FOUND", "Nguoi dung khong con ton tai."));

        if (userEntity.getStatus() == null || userEntity.getStatus() != CommonStatusConstant.STATUS_ACTIVE) {
            throw new UnauthorizedException("USER_INACTIVE", "Tai khoan dang khong hoat dong.");
        }

        RoleEntity selectedRole = userEntity.getRoles().stream()
                .filter(roleEntity -> roleEntity.getId().equals(selectedRoleId))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("ROLE_NOT_FOUND", "Role dang nhap khong con ton tai."));

        if (selectedRole.getStatus() == null || selectedRole.getStatus() != CommonStatusConstant.STATUS_ACTIVE) {
            throw new UnauthorizedException("ROLE_INACTIVE", "Role dang nhap khong con hoat dong.");
        }

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
}
