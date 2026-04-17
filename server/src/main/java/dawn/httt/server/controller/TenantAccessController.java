package dawn.httt.server.controller;

import dawn.httt.server.common.ApiResponse;
import dawn.httt.server.common.ApiResponses;
import dawn.httt.server.constant.PermissionActionConstant;
import dawn.httt.server.dto.request.CreateModulePermissionsRequest;
import dawn.httt.server.security.RequirePermission;
import dawn.httt.server.service.TenantAccessService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/tenant-access")
public class TenantAccessController {

    private final TenantAccessService tenantAccessService;

    public TenantAccessController(TenantAccessService tenantAccessService) {
        this.tenantAccessService = tenantAccessService;
    }

    @PostMapping("/roles/{roleId}/grant-module")
    @RequirePermission(resource = "role", action = PermissionActionConstant.UPDATE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> grantModuleToRole(
            @PathVariable Long roleId,
            @Valid @RequestBody CreateModulePermissionsRequest request
    ) {
        tenantAccessService.grantModuleToRole(
                roleId,
                request.getModuleCode(),
                request.getModuleName(),
                request.getResourceCode(),
                request.getResourceName(),
                request.getActions()
        );

        return ApiResponses.ok("Cap quyen module cho role thanh cong.", Map.of(
                "roleId", roleId,
                "moduleCode", request.getModuleCode().trim().toLowerCase(),
                "resourceCode", request.getResourceCode().trim().toLowerCase()
        ));
    }

    @PostMapping("/users/{userId}/roles/{roleId}")
    @RequirePermission(resource = "user", action = PermissionActionConstant.UPDATE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> assignRoleToUser(
            @PathVariable Long userId,
            @PathVariable Long roleId
    ) {
        tenantAccessService.assignRoleToUser(userId, roleId);
        return ApiResponses.ok("Gan role cho user thanh cong.", Map.of(
                "userId", userId,
                "roleId", roleId
        ));
    }
}
