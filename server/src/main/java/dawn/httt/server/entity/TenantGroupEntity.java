package dawn.httt.server.entity;

import dawn.httt.server.constant.CommonStatusConstant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Phân hệ: Khách hàng — nhóm người thuê (có thể 1 hoặc nhiều người).
 *
 * Tái sử dụng:
 *   - AuditEntity: timestamp tự động.
 *   - representativeUserId → FK về users.id (UserEntity đã có).
 *     Đây là người đại diện ký hợp đồng, không tạo user mới.
 *
 * Quan hệ:
 *   TenantGroupEntity 1 --- N TenantGroupMemberEntity (danh sách thành viên)
 *   TenantGroupEntity 1 --- N ContractEntity      (lịch sử hợp đồng)
 */
@Getter
@Setter
@Entity
@Table(
    name = "tenant_groups",
    indexes = {
        @Index(name = "idx_tenant_groups_code", columnList = "code", unique = true),
        @Index(name = "idx_tenant_groups_representative", columnList = "representative_user_id")
    }
)
public class TenantGroupEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã nhóm tự sinh, ví dụ: "TG-2025-001". */
    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    /** Tên nhóm / tên hộ gia đình. */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * FK → users.id.
     * Người đại diện pháp lý của nhóm (ký hợp đồng, nhận hoá đơn).
     * Tận dụng UserEntity đã có — không tạo bảng tenant riêng.
     */
    @Column(name = "representative_user_id", nullable = false)
    private Long representativeUserId;

    /**
     * Trạng thái nhóm — dùng CommonStatusConstant.
     * ACTIVE=1 (đang ở), INACTIVE=2 (đã rời đi).
     */
    @Column(name = "status", nullable = false)
    private Integer status = CommonStatusConstant.STATUS_ACTIVE;

    /** Ghi chú tự do. */
    @Column(name = "note", length = 1000)
    private String note;

}
