package dawn.httt.server.entity;

import dawn.httt.server.constant.CommonStatusConstant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Phân hệ: Dịch vụ — chỉ số / mức tiêu thụ thực tế theo từng tháng.
 *
 * Tái sử dụng:
 *   - AuditEntity:     timestamp tự động.
 *   - contractId   → FK về contracts.id   (biết phòng + nhóm thuê qua HĐ).
 *   - serviceTypeId → FK về service_types.id.
 *   - status dùng CommonStatusConstant (ACTIVE=đã chốt / INACTIVE=đã huỷ).
 *
 * Tính amount:
 *   - FIXED   : amount = ServiceTypeEntity.unitPrice  (quantity = 1)
 *   - METERED : amount = quantity × ServiceTypeEntity.unitPrice
 *   amount được tính và lưu tại đây để tránh tính lại khi in hoá đơn.
 *
 * Một bản ghi = một dịch vụ × một tháng × một hợp đồng.
 * Unique constraint ngăn ghi nhận trùng cùng dịch vụ trong cùng tháng.
 */
@Getter
@Setter
@Entity
@Table(
    name = "service_usages",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_service_usages_contract_type_month",
        columnNames = {"contract_id", "service_type_id", "billing_year", "billing_month"}
    ),
    indexes = {
        @Index(name = "idx_service_usages_contract_id",    columnList = "contract_id"),
        @Index(name = "idx_service_usages_month",          columnList = "billing_year, billing_month"),
        @Index(name = "idx_service_usages_room_id",        columnList = "room_id")
    }
)
public class ServiceUsageEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK → contracts.id */
    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @Column(name = "room_id")
    private Long roomId;

    /** FK → service_types.id */
    @Column(name = "service_type_id", nullable = false)
    private Long serviceTypeId;

    /** Tháng tính phí (1–12). */
    @Column(name = "billing_month", nullable = false)
    private Integer billingMonth;

    /** Năm tính phí. */
    @Column(name = "billing_year", nullable = false)
    private Integer billingYear;

    /**
     * Lượng tiêu thụ.
     * FIXED   → luôn = 1 (một tháng).
     * METERED → chỉ số thực (kWh, m³ …).
     */
    @Column(name = "quantity", nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;

    /** Thành tiền = quantity × unitPrice tại thời điểm chốt. */
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * ACTIVE=1 (đã chốt, đưa vào hoá đơn) / INACTIVE=2 (đã huỷ dòng này).
     */
    @Column(name = "status", nullable = false)
    private Integer status = CommonStatusConstant.STATUS_ACTIVE;

    @Column(name = "previous_reading")
    private Integer previousReading;

    @Column(name = "current_reading")
    private Integer currentReading;

    @Column(name = "consumption", precision = 10, scale = 3)
    private BigDecimal consumption;

    @Column(name = "reading_date")
    private LocalDate readingDate;
}
