package dawn.httt.server.repository;

import dawn.httt.server.entity.RoomEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, Long> {
    List<RoomEntity> findBySubscriptionId(Long subscriptionId);

    Optional<RoomEntity> findBySubscriptionIdAndCode(Long subscriptionId, String code);

    List<RoomEntity> findBySubscriptionIdAndStatus(Long subscriptionId, Integer status);

    List<RoomEntity> findBySubscriptionIdAndFloor(Long subscriptionId, Integer floor);

    List<RoomEntity> findByOccupiedByUserId(Long userId);
}
