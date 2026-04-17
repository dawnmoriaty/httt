package dawn.httt.server.repository;

import dawn.httt.server.entity.ServiceUsageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceUsageRepository extends JpaRepository<ServiceUsageEntity, Long> {

    List<ServiceUsageEntity> findByServiceId(Long serviceId);

    List<ServiceUsageEntity> findByRoomId(Long roomId);

    List<ServiceUsageEntity> findByUserId(Long userId);

    List<ServiceUsageEntity> findByStatus(Integer status);

    List<ServiceUsageEntity> findByPeriodStartAndPeriodEnd(LocalDate periodStart, LocalDate periodEnd);

}
