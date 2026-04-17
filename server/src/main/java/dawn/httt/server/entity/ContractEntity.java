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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "contracts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"subscription_id", "contract_number"})
})
public class ContractEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private SubscriptionEntity subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomEntity room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "contract_number", nullable = false, length = 100)
    private String contractNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "rent_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal rentAmount = BigDecimal.ZERO;

    @Column(name = "status", nullable = false)
    private Integer status = 1; // 1=DRAFT, 2=ACTIVE, 3=EXPIRED, 4=TERMINATED, 5=RENEWED

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate;

    @OneToMany(mappedBy = "contract", fetch = FetchType.LAZY)
    private Set<ContractTermEntity> terms = new LinkedHashSet<>();

    @OneToMany(mappedBy = "contract", fetch = FetchType.LAZY)
    private Set<InvoiceEntity> invoices = new LinkedHashSet<>();

    @OneToOne(mappedBy = "contract", fetch = FetchType.LAZY)
    private ContractFileEntity file;
}
