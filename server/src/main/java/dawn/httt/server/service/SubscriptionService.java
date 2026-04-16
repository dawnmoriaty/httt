package dawn.httt.server.service;

import dawn.httt.server.dto.request.SubscriptionUpsertRequest;
import dawn.httt.server.dto.response.SubscriptionResponse;
import dawn.httt.server.entity.SubscriptionEntity;
import dawn.httt.server.exception.ForbiddenException;
import dawn.httt.server.exception.NotFoundException;
import dawn.httt.server.integration.email.EmailSender;
import dawn.httt.server.repository.SubscriptionRepository;
import dawn.httt.server.security.AuthenticatedUser;
import dawn.httt.server.security.CurrentAuthenticatedUserProvider;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final EmailSender emailSender;
    private final CurrentAuthenticatedUserProvider currentAuthenticatedUserProvider;
    private final PermissionGuard permissionGuard;

    public SubscriptionService(
            SubscriptionRepository subscriptionRepository,
            EmailSender emailSender,
            CurrentAuthenticatedUserProvider currentAuthenticatedUserProvider,
            PermissionGuard permissionGuard
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.emailSender = emailSender;
        this.currentAuthenticatedUserProvider = currentAuthenticatedUserProvider;
        this.permissionGuard = permissionGuard;
    }

    public Page<SubscriptionResponse> getAll(Pageable pageable) {
        AuthenticatedUser currentUser = currentAuthenticatedUserProvider.getCurrentUser();
        if (permissionGuard.isSuperAdmin(currentUser)) {
            return subscriptionRepository.findAllByOrderByIdDesc(pageable).map(this::toResponse);
        }

        return subscriptionRepository
                .findAllByOwnerUserIdOrderByIdDesc(currentUser.getUserId(), pageable)
                .map(this::toResponse);
    }

    public SubscriptionResponse create(SubscriptionUpsertRequest request) {
        AuthenticatedUser currentUser = currentAuthenticatedUserProvider.getCurrentUser();
        SubscriptionEntity subscriptionEntity = new SubscriptionEntity();
        subscriptionEntity.setTitle(request.getTitle().trim());
        subscriptionEntity.setDescription(request.getDescription());
        subscriptionEntity.setStatus(request.getStatus());
        subscriptionEntity.setOwnerUserId(currentUser.getUserId());
        return toResponse(subscriptionRepository.save(subscriptionEntity));
    }

    public SubscriptionResponse update(Long id, SubscriptionUpsertRequest request) {
        SubscriptionEntity subscriptionEntity = getAuthorizedSubscription(id);
        subscriptionEntity.setTitle(request.getTitle().trim());
        subscriptionEntity.setDescription(request.getDescription());
        subscriptionEntity.setStatus(request.getStatus());
        return toResponse(subscriptionRepository.save(subscriptionEntity));
    }

    public void delete(Long id) {
        SubscriptionEntity subscriptionEntity = getAuthorizedSubscription(id);
        subscriptionRepository.delete(subscriptionEntity);
    }

    public Map<String, Object> importSample() {
        return Map.of(
                "message", "Import mock thanh cong.",
                "imported", 3
        );
    }

    public Map<String, Object> exportSample() {
        return Map.of(
                "message", "Export mock thanh cong.",
                "total", subscriptionRepository.count()
        );
    }

    public Map<String, Object> exportAndSendByEmail(String email) {
        long total = subscriptionRepository.count();
        String subject = "HTTT export result";
        String content = "Tong so ban ghi subscription hien tai: " + total;
        emailSender.send(email, subject, content);

        return Map.of(
                "message", "Export va gui email thanh cong.",
                "total", total,
                "recipient", email
        );
    }

    private SubscriptionResponse toResponse(SubscriptionEntity subscriptionEntity) {
        return SubscriptionResponse.builder()
                .id(subscriptionEntity.getId())
                .title(subscriptionEntity.getTitle())
                .description(subscriptionEntity.getDescription())
                .status(subscriptionEntity.getStatus())
                .ownerUserId(subscriptionEntity.getOwnerUserId())
                .build();
    }

    private SubscriptionEntity getAuthorizedSubscription(Long id) {
        AuthenticatedUser currentUser = currentAuthenticatedUserProvider.getCurrentUser();
        SubscriptionEntity subscriptionEntity = subscriptionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("SUBSCRIPTION_NOT_FOUND", "Khong tim thay ban ghi mau."));

        if (permissionGuard.isSuperAdmin(currentUser)) {
            return subscriptionEntity;
        }

        if (subscriptionEntity.getOwnerUserId() == null
                || !subscriptionEntity.getOwnerUserId().equals(currentUser.getUserId())) {
            throw new ForbiddenException("DATA_OWNERSHIP_FORBIDDEN", "Ban chi co the thao tac tren du lieu cua chinh minh.");
        }

        return subscriptionEntity;
    }
}
