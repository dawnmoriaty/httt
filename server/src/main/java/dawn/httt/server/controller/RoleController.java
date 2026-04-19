package dawn.httt.server.controller;

import dawn.httt.server.common.ApiResponse;
import dawn.httt.server.common.ApiResponses;
import dawn.httt.server.common.PageResponse;
import dawn.httt.server.constant.PermissionActionConstant;
import dawn.httt.server.dto.request.AssignRolePermissionsRequest;
import dawn.httt.server.dto.request.CreateRoleRequest;
import dawn.httt.server.dto.request.UpdateRoleRequest;
import dawn.httt.server.dto.response.RoleResponse;
import dawn.httt.server.security.RequirePermission;
import dawn.httt.server.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<ApiResponse<PageResponse<RoleResponse>>> listRoles(
            @RequestParam(name = "q", required = false) String query,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponses.ok("Lay danh sach role thanh cong.", PageResponse.from(roleService.getAllRoles(query, pageable)));
    }

    @GetMapping("/{roleId}")
    @RequirePermission(resource = "role", action = PermissionActionConstant.VIEW)
    public ResponseEntity<ApiResponse<RoleResponse>> getRole(@PathVariable Long roleId) {
        return ApiResponses.ok("Lay chi tiet role thanh cong.", roleService.getRole(roleId));
    }

    @PostMapping
    @RequirePermission(resource = "role", action = PermissionActionConstant.ADD)
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody CreateRoleRequest request) {
        return ApiResponses.created("Tao role thanh cong.", roleService.createRole(request));
    }

    @PutMapping("/{roleId}")
    @RequirePermission(resource = "role", action = PermissionActionConstant.UPDATE)
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(@PathVariable Long roleId, @Valid @RequestBody UpdateRoleRequest request) {
        return ApiResponses.ok("Cap nhat role thanh cong.", roleService.updateRole(roleId, request));
    }

    @PutMapping("/{roleId}/permissions")
    @RequirePermission(resource = "role", action = PermissionActionConstant.UPDATE)
    public ResponseEntity<ApiResponse<RoleResponse>> assignPermissions(
            @PathVariable Long roleId,
            @Valid @RequestBody AssignRolePermissionsRequest request
    ) {
        return ApiResponses.ok("Cap nhat quyen cho role thanh cong.", roleService.assignPermissions(roleId, request));
    }

    @DeleteMapping("/{roleId}")
    @RequirePermission(resource = "role", action = PermissionActionConstant.DELETE)
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return ApiResponses.noContent();
    }
}
