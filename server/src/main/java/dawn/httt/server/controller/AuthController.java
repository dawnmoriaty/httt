package dawn.httt.server.controller;

import dawn.httt.server.common.ApiResponse;
import dawn.httt.server.dto.request.LoginRequest;
import dawn.httt.server.dto.request.RefreshTokenRequest;
import dawn.httt.server.dto.request.RegisterRequest;
import dawn.httt.server.dto.response.AuthResponse;
import dawn.httt.server.dto.response.CurrentUserResponse;
import dawn.httt.server.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return new ApiResponse<>(true, "Dang ky thanh cong.", authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return new ApiResponse<>(true, "Dang nhap thanh cong.", authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return new ApiResponse<>(true, "Lam moi token thanh cong.", authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String authorization) {
        authService.logout(authorization);
        return new ApiResponse<>(true, "Dang xuat thanh cong.", null);
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me() {
        return new ApiResponse<>(true, "Lay thong tin thanh cong.", authService.currentUser());
    }
}
