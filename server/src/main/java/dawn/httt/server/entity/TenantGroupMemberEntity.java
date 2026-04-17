package dawn.httt.server.entity;

import dawn.httt.server.constant.TenantMemberRoleConstant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tenant_group_members", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_group_id", "user_id"})
})
public class TenantGroupMemberEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_group_id", nullable = false)
    private TenantGroupEntity tenantGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "member_role", nullable = false)
    private Integer memberRole = TenantMemberRoleConstant.MEMBER;

    @Column(name = "joined_at")
    private LocalDate joinedAt;

    @Column(name = "left_at")
    private LocalDate leftAt;

    @Column(name = "id_card_number", length = 50)
    private String idCardNumber;

    @Column(name = "id_card_front", length = 500)
    private String idCardFront;

    @Column(name = "id_card_back", length = 500)
    private String idCardBack;
}
