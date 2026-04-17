package dawn.httt.server.entity;

import dawn.httt.server.constant.TenantMemberRoleConstant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Phân hệ: Khách hàng — từng người trong nhóm thuê.
 *
 * Tái sử dụng:
 *   - AuditEntity: timestamp tự động.
 *   - userId → FK về users.id: tận dụng hoàn toàn UserEntity đang có
 *     (đã có username, email, fullName). Không cần lưu lại thông tin.
 *   - groupId → FK về tenant_groups.id.
 *
 * Bảng này là join-table "thông minh" (có thêm nghiệp vụ: role, ngày vào/ra)
 * nên không dùng @ManyToMany thuần mà tách thành entity riêng.
 */
@Getter
@Setter
@Entity
@Table(
    name = "tenant_members",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_tenant_members_group_user",
        columnNames = {"group_id", "user_id"}
    ),
    indexes = {
        @Index(name = "idx_tenant_members_group_id", columnList = "group_id"),
        @Index(name = "idx_tenant_members_user_id",  columnList = "user_id")
    }
)
public class TenantMemberEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK → tenant_groups.id */
    @Column(name = "group_id", nullable = false)
    private Long groupId;

    /**
     * FK → users.id.
     * Mỗi thành viên có một tài khoản user (quản lý hộ khẩu, CMND …
     * được lưu ở users.full_name / email).
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Vai trò trong nhóm — dùng TenantMemberRoleConstant.
     * REPRESENTATIVE=1 (đại diện), MEMBER=2 (người ở cùng).
     */
    @Column(name = "member_role", nullable = false)
    private Integer memberRole = TenantMemberRoleConstant.MEMBER;

    /** Ngày chính thức vào ở. */
    @Column(name = "joined_at", nullable = false)
    private LocalDate joinedAt;

    /**
     * Ngày rời đi. NULL = đang ở.
     * Cho phép lịch sử (cùng user có thể ở lại nhiều lần nếu nhóm mới).
     */
    @Column(name = "left_at")
    private LocalDate leftAt;
}
