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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "assets")
public class AssetEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomEntity room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private SubscriptionEntity subscription;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "type", nullable = false, length = 50)
    private String type; // FURNITURE, EQUIPMENT, FIXTURE, OTHER

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "value", precision = 15, scale = 2)
    private BigDecimal value = BigDecimal.ZERO;

    @Column(name = "status", nullable = false)
    private Integer status = 1; // 1=ACTIVE, 2=DAMAGED, 3=MAINTENANCE, 4=DISPOSED

    @Column(name = "description", length = 1000)
    private String description;

    @OneToMany(mappedBy = "asset", fetch = FetchType.LAZY)
    private Set<AssetMaintenanceEntity> maintenances = new LinkedHashSet<>();
}
