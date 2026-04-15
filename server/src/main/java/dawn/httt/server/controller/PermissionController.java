package dawn.httt.server.controller;

import dawn.httt.server.common.ApiResponse;
import dawn.httt.server.constant.PermissionActionConstant;
import dawn.httt.server.dto.response.PermissionResponse;
import dawn.httt.server.security.RequirePermission;
import dawn.httt.server.service.PermissionService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ApiResponse<List<PermissionResponse>> listPermissions() {
        return new ApiResponse<>(true, "Lay danh sach permission thanh cong.", permissionService.getAllPermissions());
    }
}
