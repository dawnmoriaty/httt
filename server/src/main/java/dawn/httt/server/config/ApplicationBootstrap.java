package dawn.httt.server.config;

import dawn.httt.server.constant.CommonStatusConstant;
import dawn.httt.server.constant.PermissionActionConstant;
import dawn.httt.server.constant.RoleCodeConstant;
import dawn.httt.server.entity.PermissionEntity;
import dawn.httt.server.entity.RoleEntity;
import dawn.httt.server.entity.UserEntity;
import dawn.httt.server.repository.PermissionRepository;
import dawn.httt.server.repository.RoleRepository;
import dawn.httt.server.repository.UserRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ApplicationBootstrap {

    @Bean
    public ApplicationRunner bootstrapRunner(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AppBootstrapProperties appBootstrapProperties
    ) {
        return args -> {
            seedPermissions(permissionRepository);
            Map<String, PermissionEntity> permissionMap = permissionRepository.findAll().stream()
                    .collect(Collectors.toMap(permission -> permission.getResourceCode() + ":" + permission.getActionCode(), Function.identity()));

            RoleEntity superAdminRole = seedRole(roleRepository, RoleCodeConstant.SUPER_ADMIN, "Super Admin", true);
            RoleEntity managerRole = seedRole(roleRepository, RoleCodeConstant.MANAGER, "Manager", true);
            RoleEntity userRole = seedRole(roleRepository, RoleCodeConstant.USER, "User", true);

            superAdminRole.setPermissions(new LinkedHashSet<>(permissionMap.values()));
            managerRole.setPermissions(new LinkedHashSet<>(List.of(
                    permissionMap.get("subscription:" + PermissionActionConstant.VIEW),
                    permissionMap.get("subscription:" + PermissionActionConstant.ADD),
                    permissionMap.get("subscription:" + PermissionActionConstant.UPDATE),
                    permissionMap.get("subscription:" + PermissionActionConstant.DELETE),
                    permissionMap.get("subscription:" + PermissionActionConstant.IMPORT),
                    permissionMap.get("subscription:" + PermissionActionConstant.EXPORT)
            )));
            userRole.setPermissions(new LinkedHashSet<>(List.of(
                    permissionMap.get("subscription:" + PermissionActionConstant.VIEW),
                    permissionMap.get("subscription:" + PermissionActionConstant.IMPORT),
                    permissionMap.get("subscription:" + PermissionActionConstant.EXPORT)
            )));

            roleRepository.saveAll(List.of(superAdminRole, managerRole, userRole));
            seedAdminUser(userRepository, passwordEncoder, appBootstrapProperties, superAdminRole);
        };
    }

    private void seedPermissions(PermissionRepository permissionRepository) {
        seedPermission(permissionRepository, "rbac", "RBAC", "role", "Role", PermissionActionConstant.VIEW, "Xem");
        seedPermission(permissionRepository, "rbac", "RBAC", "role", "Role", PermissionActionConstant.ADD, "Them");
        seedPermission(permissionRepository, "rbac", "RBAC", "role", "Role", PermissionActionConstant.UPDATE, "Sua");
        seedPermission(permissionRepository, "rbac", "RBAC", "role", "Role", PermissionActionConstant.DELETE, "Xoa");
        seedPermission(permissionRepository, "rbac", "RBAC", "permission", "Permission", PermissionActionConstant.VIEW, "Xem");
        seedPermission(permissionRepository, "rbac", "RBAC", "user", "User", PermissionActionConstant.VIEW, "Xem");
        seedPermission(permissionRepository, "rbac", "RBAC", "user", "User", PermissionActionConstant.ADD, "Them");
        seedPermission(permissionRepository, "rbac", "RBAC", "user", "User", PermissionActionConstant.UPDATE, "Sua");
        seedPermission(permissionRepository, "sample", "Sample Data", "subscription", "Subscription", PermissionActionConstant.VIEW, "Xem");
        seedPermission(permissionRepository, "sample", "Sample Data", "subscription", "Subscription", PermissionActionConstant.ADD, "Them");
        seedPermission(permissionRepository, "sample", "Sample Data", "subscription", "Subscription", PermissionActionConstant.UPDATE, "Sua");
        seedPermission(permissionRepository, "sample", "Sample Data", "subscription", "Subscription", PermissionActionConstant.DELETE, "Xoa");
        seedPermission(permissionRepository, "sample", "Sample Data", "subscription", "Subscription", PermissionActionConstant.IMPORT, "Nhap");
        seedPermission(permissionRepository, "sample", "Sample Data", "subscription", "Subscription", PermissionActionConstant.EXPORT, "Xuat");
    }

    private void seedPermission(
            PermissionRepository permissionRepository,
            String moduleCode,
            String moduleName,
            String resourceCode,
            String resourceName,
            String actionCode,
            String actionName
    ) {
        permissionRepository.findByResourceCodeAndActionCode(resourceCode, actionCode).orElseGet(() -> {
            PermissionEntity permissionEntity = new PermissionEntity();
            permissionEntity.setModuleCode(moduleCode);
            permissionEntity.setModuleName(moduleName);
            permissionEntity.setResourceCode(resourceCode);
            permissionEntity.setResourceName(resourceName);
            permissionEntity.setActionCode(actionCode);
            permissionEntity.setActionName(actionName);
            permissionEntity.setStatus(CommonStatusConstant.STATUS_ACTIVE);
            return permissionRepository.save(permissionEntity);
        });
    }

    private RoleEntity seedRole(RoleRepository roleRepository, String code, String name, boolean systemRole) {
        return roleRepository.findByCode(code).orElseGet(() -> {
            RoleEntity roleEntity = new RoleEntity();
            roleEntity.setCode(code);
            roleEntity.setName(name);
            roleEntity.setDescription(name + " default role");
            roleEntity.setStatus(CommonStatusConstant.STATUS_ACTIVE);
            roleEntity.setSystemRole(systemRole);
            return roleRepository.save(roleEntity);
        });
    }

    private void seedAdminUser(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AppBootstrapProperties appBootstrapProperties,
            RoleEntity superAdminRole
    ) {
        if (userRepository.existsByUsername(appBootstrapProperties.getAdminUsername())) {
            return;
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(appBootstrapProperties.getAdminUsername());
        userEntity.setFullName(appBootstrapProperties.getAdminFullName());
        userEntity.setEmail(appBootstrapProperties.getAdminEmail());
        userEntity.setPasswordHash(passwordEncoder.encode(appBootstrapProperties.getAdminPassword()));
        userEntity.setStatus(CommonStatusConstant.STATUS_ACTIVE);
        userEntity.getRoles().add(superAdminRole);
        userRepository.save(userEntity);
    }
}
