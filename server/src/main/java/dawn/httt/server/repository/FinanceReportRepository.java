package dawn.httt.server.repository;

import dawn.httt.server.entity.FinanceReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinanceReportRepository extends JpaRepository<FinanceReportEntity, Long> {

    List<FinanceReportEntity> findBySubscriptionId(Long subscriptionId);

    List<FinanceReportEntity> findByReportType(String reportType);

    Optional<FinanceReportEntity> findBySubscriptionIdAndReportType(Long subscriptionId, String reportType);

    List<FinanceReportEntity> findByPeriodStartAndPeriodEnd(LocalDate periodStart, LocalDate periodEnd);

}
