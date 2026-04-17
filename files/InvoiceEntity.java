package dawn.httt.server.entity;

import dawn.httt.server.constant.InvoiceStatusConstant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Phân hệ: Tài chính — hoá đơn tổng hợp hàng tháng cho một hợp đồng.
 *
 * Tái sử dụng:
 *   - AuditEntity:  timestamp tự động.
 *   - contractId → FK về contracts.id (biết phòng + nhóm thuê + giá thuê qua HĐ).
 *
 * Luồng tạo hoá đơn (nằm trong InvoiceService):
 *   1. Lấy ContractEntity (rentPrice).
 *   2. Tổng hợp ServiceUsageEntity cùng tháng → serviceAmount.
 *   3. totalAmount = rentAmount + serviceAmount.
 *   4. Lưu InvoiceEntity, status = UNPAID.
 *
 * Quan hệ ngược: InvoiceEntity 1 --- N PaymentEntity.
 *
 * Unique constraint: mỗi hợp đồng chỉ có 1 hoá đơn / tháng.
 */
@Getter
@Setter
@Entity
@Table(
    name = "invoices",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_invoices_contract_month",
        columnNames = {"contract_id", "billing_year", "billing_month"}
    ),
    indexes = {
        @Index(name = "idx_invoices_invoice_no",   columnList = "invoice_no",   unique = true),
        @Index(name = "idx_invoices_contract_id",  columnList = "contract_id"),
        @Index(name = "idx_invoices_status",       columnList = "status"),
        @Index(name = "idx_invoices_due_date",     columnList = "due_date")   // job nhắc nhở quá hạn
    }
)
public class InvoiceEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Số hoá đơn hiển thị, ví dụ: "INV-2025-05-001". */
    @Column(name = "invoice_no", nullable = false, unique = true, length = 30)
    private String invoiceNo;

    /** FK → contracts.id */
    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    /** Tháng tính phí (1–12). */
    @Column(name = "billing_month", nullable = false)
    private Integer billingMonth;

    /** Năm tính phí. */
    @Column(name = "billing_year", nullable = false)
    private Integer billingYear;

    /**
     * Tiền thuê phòng tháng này — snapshot từ ContractEntity.rentPrice.
     * Lưu snapshot để hoá đơn không thay đổi nếu hợp đồng điều chỉnh sau.
     */
    @Column(name = "rent_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal rentAmount;

    /** Tổng tiền dịch vụ tháng này — tổng từ ServiceUsageEntity. */
    @Column(name = "service_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal serviceAmount = BigDecimal.ZERO;

    /** Tổng cộng = rentAmount + serviceAmount. */
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    /** Hạn thanh toán. */
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /**
     * Trạng thái — dùng InvoiceStatusConstant.
     * UNPAID=1, PAID=2, OVERDUE=3, CANCELLED=4
     */
    @Column(name = "status", nullable = false)
    private Integer status = InvoiceStatusConstant.UNPAID;

    /** Ghi chú (giảm giá, phụ thu …). */
    @Column(name = "note", length = 1000)
    private String note;
}
