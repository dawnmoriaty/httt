package dawn.httt.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "services", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"subscription_id", "code"})
})
public class ServiceEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private SubscriptionEntity subscription;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "type", nullable = false, length = 50)
    private String type; // WIFI, ELECTRIC, WATER, CLEANING, OTHER

    @Column(name = "unit", length = 50)
    private String unit; // kWh, m3, times, etc

    @Column(name = "base_price", precision = 15, scale = 2)
    private BigDecimal basePrice = BigDecimal.ZERO;

    @Column(name = "status", nullable = false)
    private Integer status = 1; // 1=ACTIVE, 2=INACTIVE

    @Column(name = "description", length = 1000)
    private String description;

    @OneToMany(mappedBy = "service", fetch = FetchType.LAZY)
    private Set<ServiceUsageEntity> usages = new LinkedHashSet<>();
}
