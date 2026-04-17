package dawn.httt.server.repository;

import dawn.httt.server.entity.TenantGroupEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantGroupRepository extends JpaRepository<TenantGroupEntity, Long> {
    List<TenantGroupEntity> findBySubscriptionId(Long subscriptionId);

    Optional<TenantGroupEntity> findBySubscriptionIdAndName(Long subscriptionId, String name);
}
