package dawn.httt.server.repository;

import dawn.httt.server.entity.AssetEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<AssetEntity, Long> {
    List<AssetEntity> findByRoomId(Long roomId);

    List<AssetEntity> findBySubscriptionId(Long subscriptionId);

    List<AssetEntity> findBySubscriptionIdAndType(Long subscriptionId, String type);

    List<AssetEntity> findByRoomIdAndStatus(Long roomId, Integer status);
}
