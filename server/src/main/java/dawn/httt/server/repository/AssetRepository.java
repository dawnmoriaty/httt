package dawn.httt.server.repository;

import dawn.httt.server.entity.AssetEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<AssetEntity, Long> {
    List<AssetEntity> findByRoomId(Long roomId);

    List<AssetEntity> findByAssetType(Integer assetType);

    List<AssetEntity> findByRoomIdAndAssetType(Long roomId, Integer assetType);

    List<AssetEntity> findByRoomIdAndConditionStatus(Long roomId, Integer conditionStatus);

    List<AssetEntity> findByConditionStatus(Integer conditionStatus);
}
