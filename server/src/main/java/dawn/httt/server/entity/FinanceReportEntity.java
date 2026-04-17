package dawn.httt.server.entity;

import java.time.Instant;
import java.time.YearMonth;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "finance_reports", indexes = {
    @Index(name = "idx_finance_reports_report_month", columnList = "report_month", unique = true)
})
public class FinanceReportEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_month", nullable = false, length = 7)
    private YearMonth reportMonth;

    @Column(name = "total_revenue", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "total_paid", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPaid = BigDecimal.ZERO;

    @Column(name = "total_outstanding", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalOutstanding = BigDecimal.ZERO;

    @Column(name = "total_invoices", nullable = false)
    private Integer totalInvoices = 0;

    @Column(name = "paid_invoices", nullable = false)
    private Integer paidInvoices = 0;

    @Column(name = "overdue_invoices", nullable = false)
    private Integer overdueInvoices = 0;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;
}
