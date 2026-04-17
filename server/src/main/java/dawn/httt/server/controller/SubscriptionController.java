package dawn.httt.server.controller;

import dawn.httt.server.common.ApiResponse;
import dawn.httt.server.common.ApiResponses;
import dawn.httt.server.constant.PermissionActionConstant;
import dawn.httt.server.dto.request.SubscriptionUpsertRequest;
import dawn.httt.server.dto.response.SubscriptionResponse;
import dawn.httt.server.security.RequirePermission;
import dawn.httt.server.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<ApiResponse<Page<SubscriptionResponse>>> listSubscriptions(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponses.ok("Lay danh sach tenant subscription thanh cong.", subscriptionService.getAll(pageable));
    }

    @PostMapping
    @RequirePermission(resource = "subscription", action = PermissionActionConstant.ADD)
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscription(@Valid @RequestBody SubscriptionUpsertRequest request) {
        return ApiResponses.created("Tao tenant subscription thanh cong.", subscriptionService.create(request));
    }

    @PutMapping("/{id}")
    @RequirePermission(resource = "subscription", action = PermissionActionConstant.UPDATE)
    public ResponseEntity<ApiResponse<SubscriptionResponse>> updateSubscription(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionUpsertRequest request
    ) {
        return ApiResponses.ok("Cap nhat tenant subscription thanh cong.", subscriptionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequirePermission(resource = "subscription", action = PermissionActionConstant.DELETE)
    public ResponseEntity<Void> deleteSubscription(@PathVariable Long id) {
        subscriptionService.delete(id);
        return ApiResponses.noContent();
    }
}
