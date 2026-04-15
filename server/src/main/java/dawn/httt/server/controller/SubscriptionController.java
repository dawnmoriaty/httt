package dawn.httt.server.controller;

import dawn.httt.server.common.ApiResponse;
import dawn.httt.server.constant.PermissionActionConstant;
import dawn.httt.server.dto.request.SubscriptionExportMailRequest;
import dawn.httt.server.dto.request.SubscriptionUpsertRequest;
import dawn.httt.server.dto.response.SubscriptionResponse;
import dawn.httt.server.security.RequirePermission;
import dawn.httt.server.service.SubscriptionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping
    @RequirePermission(resource = "subscription", action = PermissionActionConstant.VIEW)
    public ApiResponse<List<SubscriptionResponse>> listSubscriptions() {
        return new ApiResponse<>(true, "Lay danh sach ban ghi mau thanh cong.", subscriptionService.getAll());
    }

    @PostMapping
    @RequirePermission(resource = "subscription", action = PermissionActionConstant.ADD)
    public ApiResponse<SubscriptionResponse> createSubscription(@Valid @RequestBody SubscriptionUpsertRequest request) {
        return new ApiResponse<>(true, "Tao ban ghi mau thanh cong.", subscriptionService.create(request));
    }

    @PutMapping("/{id}")
    @RequirePermission(resource = "subscription", action = PermissionActionConstant.UPDATE)
    public ApiResponse<SubscriptionResponse> updateSubscription(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionUpsertRequest request
    ) {
        return new ApiResponse<>(true, "Cap nhat ban ghi mau thanh cong.", subscriptionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequirePermission(resource = "subscription", action = PermissionActionConstant.DELETE)
    public ApiResponse<Void> deleteSubscription(@PathVariable Long id) {
        subscriptionService.delete(id);
        return new ApiResponse<>(true, "Xoa ban ghi mau thanh cong.", null);
    }

    @PostMapping("/import")
    @RequirePermission(resource = "subscription", action = PermissionActionConstant.IMPORT)
    public ApiResponse<Map<String, Object>> importSubscription() {
        return new ApiResponse<>(true, "Import mock thanh cong.", subscriptionService.importSample());
    }

    @GetMapping("/export")
    @RequirePermission(resource = "subscription", action = PermissionActionConstant.EXPORT)
    public ApiResponse<Map<String, Object>> exportSubscription() {
        return new ApiResponse<>(true, "Export mock thanh cong.", subscriptionService.exportSample());
    }

    @PostMapping("/export/mail")
    @RequirePermission(resource = "subscription", action = PermissionActionConstant.EXPORT)
    public ApiResponse<Map<String, Object>> exportSubscriptionToMail(
            @Valid @RequestBody SubscriptionExportMailRequest request
    ) {
        return new ApiResponse<>(
                true,
                "Export va gui email thanh cong.",
                subscriptionService.exportAndSendByEmail(request.getEmail())
        );
    }
}
