package dawn.httt.server.repository;

import dawn.httt.server.entity.CameraEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CameraRepository extends JpaRepository<CameraEntity, Long> {

    List<CameraEntity> findBySubscriptionId(Long subscriptionId);

    List<CameraEntity> findByRoomId(Long roomId);

    Optional<CameraEntity> findBySubscriptionIdAndCode(Long subscriptionId, String code);

    List<CameraEntity> findByStatus(Integer status);

}
