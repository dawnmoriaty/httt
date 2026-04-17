package dawn.httt.server.controller;

import dawn.httt.server.common.ApiResponse;
import dawn.httt.server.common.ApiResponses;
import dawn.httt.server.constant.PermissionActionConstant;
import dawn.httt.server.dto.request.CreatePermissionRequest;
import dawn.httt.server.dto.response.PermissionResponse;
import dawn.httt.server.security.RequirePermission;
import dawn.httt.server.service.PermissionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    @RequirePermission(resource = "permission", action = PermissionActionConstant.VIEW)
    public ResponseEntity<ApiResponse<Page<PermissionResponse>>> listPermissions(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponses.ok("Lay danh sach permission thanh cong.", permissionService.getAllPermissions(pageable));
    }

    @PostMapping
    @RequirePermission(resource = "permission", action = PermissionActionConstant.ADD)
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        return ApiResponses.created("Tao permission thanh cong.", permissionService.createPermission(request));
    }
}
