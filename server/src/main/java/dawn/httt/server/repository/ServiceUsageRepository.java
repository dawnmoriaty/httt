package dawn.httt.server.repository;

import dawn.httt.server.entity.ServiceUsageEntity;
import java.time.YearMonth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceUsageRepository extends JpaRepository<ServiceUsageEntity, Long> {

    List<ServiceUsageEntity> findByContractId(Long contractId);

    List<ServiceUsageEntity> findByServiceTypeId(Long serviceTypeId);

    List<ServiceUsageEntity> findByStatus(Integer status);

    List<ServiceUsageEntity> findByBillingYearAndBillingMonth(Integer billingYear, Integer billingMonth);

    List<ServiceUsageEntity> findByRoomId(Long roomId);

    List<ServiceUsageEntity> findByBillingPeriod(YearMonth billingPeriod);

}
