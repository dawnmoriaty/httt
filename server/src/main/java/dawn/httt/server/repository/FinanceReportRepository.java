package dawn.httt.server.repository;

import dawn.httt.server.entity.FinanceReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinanceReportRepository extends JpaRepository<FinanceReportEntity, Long> {

    Optional<FinanceReportEntity> findByReportMonth(YearMonth reportMonth);

    List<FinanceReportEntity> findByOverdueInvoicesGreaterThan(Integer overdueInvoices);

    List<FinanceReportEntity> findByTotalOutstandingGreaterThan(java.math.BigDecimal amount);

    List<FinanceReportEntity> findByTotalRevenueGreaterThanEqual(java.math.BigDecimal minimumRevenue);

}
