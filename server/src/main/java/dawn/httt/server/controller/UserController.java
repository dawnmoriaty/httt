package dawn.httt.server.controller;

import dawn.httt.server.common.ApiResponse;
import dawn.httt.server.constant.PermissionActionConstant;
import dawn.httt.server.dto.request.CreateUserRequest;
import dawn.httt.server.dto.request.UpdateUserRolesRequest;
import dawn.httt.server.dto.response.UserResponse;
import dawn.httt.server.security.RequirePermission;
import dawn.httt.server.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @RequirePermission(resource = "user", action = PermissionActionConstant.VIEW)
    public ApiResponse<List<UserResponse>> listUsers() {
        return new ApiResponse<>(true, "Lay danh sach user thanh cong.", userService.getAllUsers());
    }

    @PostMapping
    @RequirePermission(resource = "user", action = PermissionActionConstant.ADD)
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return new ApiResponse<>(true, "Tao user thanh cong.", userService.createUser(request));
    }

    @PutMapping("/{userId}/roles")
    @RequirePermission(resource = "user", action = PermissionActionConstant.UPDATE)
    public ApiResponse<UserResponse> updateUserRoles(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRolesRequest request
    ) {
        return new ApiResponse<>(true, "Cap nhat role cho user thanh cong.", userService.updateUserRoles(userId, request));
    }
}
