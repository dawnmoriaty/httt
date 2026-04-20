package dawn.httt.server.entity;

import dawn.httt.server.constant.ContractStatusConstant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Phân hệ: Hợp đồng — trung tâm liên kết toàn bộ hệ thống.
 *
 * Tái sử dụng:
 *   - AuditEntity:         timestamp tự động.
 *   - roomId          → FK về rooms.id       (Cơ sở vật chất).
 *   - tenantGroupId   → FK về tenant_groups.id (Khách hàng).
 *   - createdByUserId → FK về users.id        (nhân viên tạo HĐ, dùng UserEntity).
 *
 * Quan hệ ngược:
 *   ContractEntity 1 --- N ServiceUsageEntity  (Dịch vụ)
 *   ContractEntity 1 --- N InvoiceEntity        (Tài chính)
 *
 * Khi ContractEntity chuyển sang ACTIVE → RoomEntity.status = OCCUPIED.
 * Khi ContractEntity chuyển sang EXPIRED/TERMINATED → RoomEntity.status = VACANT.
 * Logic này nằm trong ContractService, không hard-code ở đây.
 */
@Getter
@Setter
@Entity
@Table(
    name = "contracts",
    indexes = {
        @Index(name = "idx_contracts_contract_no",    columnList = "contract_no",    unique = true),
        @Index(name = "idx_contracts_room_id",         columnList = "room_id"),
        @Index(name = "idx_contracts_tenant_group_id", columnList = "tenant_group_id"),
        @Index(name = "idx_contracts_status",          columnList = "status"),
        @Index(name = "idx_contracts_end_date",        columnList = "end_date")   // phục vụ job cảnh báo sắp hết hạn
    }
)
public class ContractEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Số hợp đồng hiển thị, ví dụ: "HD-2025-001". */
    @Column(name = "contract_no", nullable = false, unique = true, length = 30)
    private String contractNo;

    /** FK → rooms.id */
    @Column(name = "room_id", nullable = false)
    private Long roomId;

    /** FK → tenant_groups.id */
    @Column(name = "tenant_group_id", nullable = false)
    private Long tenantGroupId;

    /**
     * FK → users.id — nhân viên quản lý lập hợp đồng.
     * Tận dụng UserEntity + RBAC (chỉ user có permission contract:ADD mới tạo được).
     */
    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    /**
     * Giá thuê đàm phán thực tế (VND/tháng).
     * Có thể khác RoomEntity.basePrice khi có khuyến mãi.
     */
    @Column(name = "rent_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal rentPrice;

    @Column(name = "deposit_amount", precision = 15, scale = 2)
    private BigDecimal depositAmount = BigDecimal.ZERO;

    /** Ngày bắt đầu hiệu lực. */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /** Ngày kết thúc hợp đồng. */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * Trạng thái — dùng ContractStatusConstant.
     * DRAFT=1, ACTIVE=2, EXPIRED=3, TERMINATED=4
     */
    @Column(name = "status", nullable = false)
    private Integer status = ContractStatusConstant.DRAFT;

    /** Điều khoản / ghi chú bổ sung. */
    @Column(name = "note", length = 2000)
    private String note;
}
