package dawn.httt.server.service;

import dawn.httt.server.dto.request.SubscriptionUpsertRequest;
import dawn.httt.server.dto.response.SubscriptionResponse;
import dawn.httt.server.entity.SubscriptionEntity;
import dawn.httt.server.exception.NotFoundException;
import dawn.httt.server.integration.email.EmailSender;
import dawn.httt.server.repository.SubscriptionRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final EmailSender emailSender;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, EmailSender emailSender) {
        this.subscriptionRepository = subscriptionRepository;
        this.emailSender = emailSender;
    }

    public List<SubscriptionResponse> getAll() {
        return subscriptionRepository.findAllByOrderByIdDesc().stream().map(this::toResponse).toList();
    }

    public SubscriptionResponse create(SubscriptionUpsertRequest request) {
        SubscriptionEntity subscriptionEntity = new SubscriptionEntity();
        subscriptionEntity.setTitle(request.getTitle().trim());
        subscriptionEntity.setDescription(request.getDescription());
        subscriptionEntity.setStatus(request.getStatus());
        return toResponse(subscriptionRepository.save(subscriptionEntity));
    }

    public SubscriptionResponse update(Long id, SubscriptionUpsertRequest request) {
        SubscriptionEntity subscriptionEntity = subscriptionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("SUBSCRIPTION_NOT_FOUND", "Khong tim thay ban ghi mau."));
        subscriptionEntity.setTitle(request.getTitle().trim());
        subscriptionEntity.setDescription(request.getDescription());
        subscriptionEntity.setStatus(request.getStatus());
        return toResponse(subscriptionRepository.save(subscriptionEntity));
    }

    public void delete(Long id) {
        SubscriptionEntity subscriptionEntity = subscriptionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("SUBSCRIPTION_NOT_FOUND", "Khong tim thay ban ghi mau."));
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
                .build();
    }
}
