package dawn.httt.server.service;

import dawn.httt.server.constant.CommonStatusConstant;
import dawn.httt.server.dto.request.AssignRolePermissionsRequest;
import dawn.httt.server.dto.request.CreateRoleRequest;
import dawn.httt.server.dto.request.UpdateRoleRequest;
import dawn.httt.server.dto.response.RoleResponse;
import dawn.httt.server.entity.PermissionEntity;
import dawn.httt.server.entity.RoleEntity;
import dawn.httt.server.exception.BadRequestException;
import dawn.httt.server.exception.NotFoundException;
import dawn.httt.server.repository.PermissionRepository;
import dawn.httt.server.repository.RoleRepository;
import dawn.httt.server.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final AuthSessionService authSessionService;
    private final UserService userService;
    private final PermissionGuard permissionGuard;

    public RoleService(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            UserRepository userRepository,
            AuthSessionService authSessionService,
            UserService userService,
            PermissionGuard permissionGuard
    ) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
        this.authSessionService = authSessionService;
        this.userService = userService;
        this.permissionGuard = permissionGuard;
    }

    public Page<RoleResponse> getAllRoles(Pageable pageable) {
        return roleRepository.findAllByOrderByNameAsc(pageable).map(this::toResponse);
    }

    public RoleResponse getRole(Long roleId) {
        return toResponse(getRoleEntity(roleId));
    }

    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        String normalizedCode = request.getCode().trim().toUpperCase();
        if (roleRepository.existsByCode(normalizedCode)) {
            throw new BadRequestException("ROLE_CODE_EXISTS", "Role code da ton tai.");
        }

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setCode(normalizedCode);
        roleEntity.setName(request.getName().trim());
        roleEntity.setDescription(request.getDescription());
        roleEntity.setStatus(CommonStatusConstant.STATUS_ACTIVE);
        roleEntity.setSystemRole(false);
        return toResponse(roleRepository.save(roleEntity));
    }

    @Transactional
    public RoleResponse updateRole(Long roleId, UpdateRoleRequest request) {
        RoleEntity roleEntity = getRoleEntity(roleId);
        Integer oldStatus = roleEntity.getStatus();

        roleEntity.setName(request.getName().trim());
        roleEntity.setDescription(request.getDescription());
        roleEntity.setStatus(request.getStatus());
        RoleEntity savedRole = roleRepository.save(roleEntity);

        if (!oldStatus.equals(request.getStatus())) {
            userRepository.findDistinctIdsByRoleId(roleId).forEach(userService::bumpSessionVersion);
            userRepository.findDistinctIdsByRoleId(roleId).forEach(authSessionService::invalidateUserSessions);
        }

        return toResponse(savedRole);
    }

    @Transactional
    public void deleteRole(Long roleId) {
        RoleEntity roleEntity = getRoleEntity(roleId);
        if (Boolean.TRUE.equals(roleEntity.getSystemRole())) {
            throw new BadRequestException("SYSTEM_ROLE_DELETE_FORBIDDEN", "Khong the xoa system role.");
        }
        if (userRepository.existsByRoles_Id(roleId)) {
            throw new BadRequestException("ROLE_IN_USE", "Role dang duoc gan cho user.");
        }

        roleRepository.delete(roleEntity);
    }

    @Transactional
    public RoleResponse assignPermissions(Long roleId, AssignRolePermissionsRequest request) {
        RoleEntity roleEntity = getRoleEntity(roleId);
        List<PermissionEntity> permissionEntities = permissionRepository.findAllByIdIn(request.getPermissionIds());
        if (permissionEntities.size() != request.getPermissionIds().size()) {
            throw new BadRequestException("PERMISSION_NOT_FOUND", "Co permission khong ton tai.");
        }

        roleEntity.setPermissions(new LinkedHashSet<>(permissionEntities));
        RoleEntity savedRole = roleRepository.save(roleEntity);
        userRepository.findDistinctIdsByRoleId(roleId).forEach(userService::bumpSessionVersion);
        userRepository.findDistinctIdsByRoleId(roleId).forEach(authSessionService::invalidateUserSessions);
        return toResponse(savedRole);
    }

    private RoleEntity getRoleEntity(Long roleId) {
        return roleRepository.findWithPermissionsById(roleId)
                .orElseThrow(() -> new NotFoundException("ROLE_NOT_FOUND", "Khong tim thay role."));
    }

    private RoleResponse toResponse(RoleEntity roleEntity) {
        return RoleResponse.builder()
                .id(roleEntity.getId())
                .code(roleEntity.getCode())
                .name(roleEntity.getName())
                .description(roleEntity.getDescription())
                .status(roleEntity.getStatus())
                .systemRole(roleEntity.getSystemRole())
                .permissionIds(roleEntity.getPermissions().stream().map(PermissionEntity::getId).toList())
                .permissionKeys(roleEntity.getPermissions().stream()
                        .map(permission -> permissionGuard.toPermissionKey(permission.getResourceCode(), permission.getActionCode()))
                        .toList())
                .build();
    }
}
