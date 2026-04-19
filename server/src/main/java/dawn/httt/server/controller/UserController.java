package dawn.httt.server.controller;

import dawn.httt.server.common.ApiResponse;
import dawn.httt.server.common.ApiResponses;
import dawn.httt.server.common.PageResponse;
import dawn.httt.server.constant.PermissionActionConstant;
import dawn.httt.server.dto.request.CreateUserRequest;
import dawn.httt.server.dto.request.UpdateUserRolesRequest;
import dawn.httt.server.dto.response.UserResponse;
import dawn.httt.server.security.RequirePermission;
import dawn.httt.server.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> listUsers(
            @RequestParam(name = "q", required = false) String query,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponses.ok("Lay danh sach user thanh cong.", PageResponse.from(userService.getAllUsers(query, pageable)));
    }

    @PostMapping
    @RequirePermission(resource = "user", action = PermissionActionConstant.ADD)
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponses.created("Tao user thanh cong.", userService.createUser(request));
    }

    @PutMapping("/{userId}/roles")
    @RequirePermission(resource = "user", action = PermissionActionConstant.UPDATE)
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRoles(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRolesRequest request
    ) {
        return ApiResponses.ok("Cap nhat role cho user thanh cong.", userService.updateUserRoles(userId, request));
    }
}
