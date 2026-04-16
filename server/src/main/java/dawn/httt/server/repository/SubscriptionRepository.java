package dawn.httt.server.repository;

import dawn.httt.server.entity.SubscriptionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {

    Page<SubscriptionEntity> findAllByOrderByIdDesc(Pageable pageable);
}
