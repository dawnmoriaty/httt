package dawn.httt.server.entity;

import dawn.httt.server.constant.CommonStatusConstant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class UserEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "status", nullable = false)
    private Integer status = CommonStatusConstant.STATUS_ACTIVE;

    @Column(name = "session_version", nullable = false)
    private Long sessionVersion = 1L;

    // New Tenant-related fields
    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "id_card", length = 50)
    private String idCard;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "relationship_to_owner", length = 50)
    private String relationshipToOwner; // OWNER, MANAGER, TENANT, GUEST

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private SubscriptionEntity subscription;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roles = new LinkedHashSet<>();

    // Relationships for tenant-related data
    @OneToMany(mappedBy = "occupiedByUser", fetch = FetchType.LAZY)
    private Set<RoomEntity> occupiedRooms = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<ServiceUsageEntity> serviceUsages = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<InvoiceEntity> invoices = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<PaymentEntity> payments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<ContractEntity> contracts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "leader", fetch = FetchType.LAZY)
    private Set<TenantGroupEntity> ledTenantGroups = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<TenantGroupMemberEntity> tenantGroupMemberships = new LinkedHashSet<>();
}
