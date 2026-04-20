package dawn.httt.server.repository;

import dawn.httt.server.entity.RoomEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, Long> {
    Optional<RoomEntity> findByCode(String code);

    List<RoomEntity> findByStatus(Integer status);

    List<RoomEntity> findByFloor(Integer floor);

    List<RoomEntity> findByStatusAndFloor(Integer status, Integer floor);

    List<RoomEntity> findByActiveContractId(Long contractId);
}
