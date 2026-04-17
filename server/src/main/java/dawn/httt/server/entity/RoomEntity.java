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
@Table(name = "rooms", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"subscription_id", "code"})
})
public class RoomEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private SubscriptionEntity subscription;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "floor", nullable = false)
    private Integer floor;

    @Column(name = "capacity", nullable = false)
    private Integer capacity = 1;

    @Column(name = "rent_price", precision = 15, scale = 2)
    private BigDecimal rentPrice = BigDecimal.ZERO;

    @Column(name = "status", nullable = false)
    private Integer status = 1; // 1=AVAILABLE, 2=OCCUPIED, 3=MAINTENANCE, 4=INACTIVE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "occupied_by_user_id")
    private UserEntity occupiedByUser;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private Set<AssetEntity> assets = new LinkedHashSet<>();

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private Set<CameraEntity> cameras = new LinkedHashSet<>();

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private Set<ContractEntity> contracts = new LinkedHashSet<>();
}
