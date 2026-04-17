package dawn.httt.server.service;

import dawn.httt.server.dto.request.AssignRolePermissionsRequest;
import dawn.httt.server.entity.RoleEntity;
import dawn.httt.server.entity.UserEntity;
import dawn.httt.server.exception.ForbiddenException;
import dawn.httt.server.exception.NotFoundException;
import dawn.httt.server.repository.RoleRepository;
import dawn.httt.server.repository.UserRepository;
import dawn.httt.server.security.AuthenticatedUser;
import dawn.httt.server.security.CurrentAuthenticatedUserProvider;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantAccessService {

    private final PermissionService permissionService;
    private final RoleService roleService;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CurrentAuthenticatedUserProvider currentAuthenticatedUserProvider;
    private final PermissionGuard permissionGuard;

    public TenantAccessService(
            PermissionService permissionService,
            RoleService roleService,
            RoleRepository roleRepository,
            UserRepository userRepository,
            CurrentAuthenticatedUserProvider currentAuthenticatedUserProvider,
            PermissionGuard permissionGuard
    ) {
        this.permissionService = permissionService;
        this.roleService = roleService;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.currentAuthenticatedUserProvider = currentAuthenticatedUserProvider;
        this.permissionGuard = permissionGuard;
    }

    @Transactional
    public void grantModuleToRole(Long roleId, String moduleCode, String moduleName, String resourceCode, String resourceName) {
        grantModuleToRole(roleId, moduleCode, moduleName, resourceCode, resourceName, null);
    }

    @Transactional
    public void grantModuleToRole(
            Long roleId,
            String moduleCode,
            String moduleName,
            String resourceCode,
            String resourceName,
            List<dawn.httt.server.dto.request.CreateModulePermissionsRequest.ActionItem> actions
    ) {
        ensureSuperAdmin();

        List<Long> permissionIds = permissionService.ensureModulePermissions(
                moduleCode,
                moduleName,
                resourceCode,
                resourceName,
                actions
        );

        RoleEntity roleEntity = roleRepository.findWithPermissionsById(roleId)
                .orElseThrow(() -> new NotFoundException("ROLE_NOT_FOUND", "Khong tim thay role."));

        Set<Long> mergedPermissionIds = new LinkedHashSet<>();
        for (dawn.httt.server.entity.PermissionEntity permission : roleEntity.getPermissions()) {
            mergedPermissionIds.add(permission.getId());
        }
        mergedPermissionIds.addAll(permissionIds);

        AssignRolePermissionsRequest request = new AssignRolePermissionsRequest();
        request.setPermissionIds(List.copyOf(mergedPermissionIds));
        roleService.assignPermissions(roleId, request);
    }

    @Transactional
    public void assignRoleToUser(Long userId, Long roleId) {
        ensureSuperAdmin();

        UserEntity userEntity = userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "Khong tim thay user."));
        RoleEntity roleEntity = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("ROLE_NOT_FOUND", "Khong tim thay role."));

        userEntity.getRoles().add(roleEntity);
        userRepository.save(userEntity);
    }

    private void ensureSuperAdmin() {
        AuthenticatedUser currentUser = currentAuthenticatedUserProvider.getCurrentUser();
        if (!permissionGuard.isSuperAdmin(currentUser)) {
            throw new ForbiddenException("FORBIDDEN", "Chi SUPER_ADMIN moi duoc cap quyen module.");
        }
    }
}
