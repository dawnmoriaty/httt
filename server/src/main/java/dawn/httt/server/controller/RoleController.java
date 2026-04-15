package dawn.httt.server.controller;

import dawn.httt.server.common.ApiResponse;
import dawn.httt.server.constant.PermissionActionConstant;
import dawn.httt.server.dto.request.AssignRolePermissionsRequest;
import dawn.httt.server.dto.request.CreateRoleRequest;
import dawn.httt.server.dto.request.UpdateRoleRequest;
import dawn.httt.server.dto.response.RoleResponse;
import dawn.httt.server.security.RequirePermission;
import dawn.httt.server.service.RoleService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @RequirePermission(resource = "role", action = PermissionActionConstant.VIEW)
    public ApiResponse<List<RoleResponse>> listRoles() {
        return new ApiResponse<>(true, "Lay danh sach role thanh cong.", roleService.getAllRoles());
    }

    @GetMapping("/{roleId}")
    @RequirePermission(resource = "role", action = PermissionActionConstant.VIEW)
    public ApiResponse<RoleResponse> getRole(@PathVariable Long roleId) {
        return new ApiResponse<>(true, "Lay chi tiet role thanh cong.", roleService.getRole(roleId));
    }

    @PostMapping
    @RequirePermission(resource = "role", action = PermissionActionConstant.ADD)
    public ApiResponse<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        return new ApiResponse<>(true, "Tao role thanh cong.", roleService.createRole(request));
    }

    @PutMapping("/{roleId}")
    @RequirePermission(resource = "role", action = PermissionActionConstant.UPDATE)
    public ApiResponse<RoleResponse> updateRole(@PathVariable Long roleId, @Valid @RequestBody UpdateRoleRequest request) {
        return new ApiResponse<>(true, "Cap nhat role thanh cong.", roleService.updateRole(roleId, request));
    }

    @PutMapping("/{roleId}/permissions")
    @RequirePermission(resource = "role", action = PermissionActionConstant.UPDATE)
    public ApiResponse<RoleResponse> assignPermissions(
            @PathVariable Long roleId,
            @Valid @RequestBody AssignRolePermissionsRequest request
    ) {
        return new ApiResponse<>(true, "Cap nhat quyen cho role thanh cong.", roleService.assignPermissions(roleId, request));
    }

    @DeleteMapping("/{roleId}")
    @RequirePermission(resource = "role", action = PermissionActionConstant.DELETE)
    public ApiResponse<Void> deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return new ApiResponse<>(true, "Xoa role thanh cong.", null);
    }
}
