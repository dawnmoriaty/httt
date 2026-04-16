package dawn.httt.server.repository;

import dawn.httt.server.entity.SubscriptionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {

    Page<SubscriptionEntity> findAllByOrderByIdDesc(Pageable pageable);

    Page<SubscriptionEntity> findAllByOwnerUserIdOrderByIdDesc(Long ownerUserId, Pageable pageable);

    Optional<SubscriptionEntity> findByIdAndOwnerUserId(Long id, Long ownerUserId);
}
