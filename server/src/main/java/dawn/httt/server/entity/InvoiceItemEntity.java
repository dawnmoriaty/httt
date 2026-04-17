package dawn.httt.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "invoice_items",
    indexes = {
        @Index(name = "idx_invoice_items_invoice_id", columnList = "invoice_id"),
        @Index(name = "idx_invoice_items_service_usage_id", columnList = "service_usage_id")
    }
)
public class InvoiceItemEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private InvoiceEntity invoice;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "billing_type", nullable = false)
    private Integer billingType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_usage_id")
    private ServiceUsageEntity serviceUsage;
}
