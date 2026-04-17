package dawn.httt.server.repository;

import dawn.httt.server.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {

    List<ServiceEntity> findBySubscriptionId(Long subscriptionId);

    Optional<ServiceEntity> findBySubscriptionIdAndCode(Long subscriptionId, String code);

    List<ServiceEntity> findByType(String type);

    List<ServiceEntity> findBySubscriptionIdAndStatus(Long subscriptionId, Integer status);

}
