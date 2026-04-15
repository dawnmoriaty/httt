package dawn.httt.server.controller;

import dawn.httt.server.common.ApiResponse;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
public class PublicController {

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        return new ApiResponse<>(true, "OK", Map.of(
                "status", "UP",
                "service", "server",
                "feature", "RBAC bootstrap"
        ));
    }
}
