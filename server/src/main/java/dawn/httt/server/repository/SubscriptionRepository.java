package dawn.httt.server.repository;

import dawn.httt.server.entity.SubscriptionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {

    List<SubscriptionEntity> findAllByOrderByIdDesc();
}
