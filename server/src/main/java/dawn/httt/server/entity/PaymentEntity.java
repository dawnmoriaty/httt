package dawn.httt.server.entity;

import dawn.httt.server.constant.PaymentMethodConstant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Phân hệ: Tài chính — từng lần thanh toán cho một hoá đơn.
 *
 * Tái sử dụng:
 *   - AuditEntity:        timestamp tự động.
 *   - invoiceId       → FK về invoices.id.
 *   - receivedByUserId → FK về users.id (nhân viên thu tiền — tận dụng UserEntity).
 *
 * Thiết kế hỗ trợ thanh toán nhiều lần cho một hoá đơn
 * (ví dụ: đặt cọc trước, trả đủ sau).
 * InvoiceService sẽ tổng hợp sum(amount) so với invoice.totalAmount
 * để cập nhật invoice.status → PAID.
 */
@Getter
@Setter
@Entity
@Table(
    name = "payments",
    indexes = {
        @Index(name = "idx_payments_invoice_id",        columnList = "invoice_id"),
        @Index(name = "idx_payments_received_by",       columnList = "received_by_user_id"),
        @Index(name = "idx_payments_paid_at",           columnList = "paid_at")
    }
)
public class PaymentEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK → invoices.id */
    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    /**
     * FK → users.id — nhân viên ghi nhận thu tiền.
     * Tận dụng UserEntity + RBAC (chỉ user có permission finance:ADD mới thu được).
     */
    @Column(name = "received_by_user_id", nullable = false)
    private Long receivedByUserId;

    /** Số tiền thu được lần này (VND). */
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * Phương thức thanh toán — dùng PaymentMethodConstant.
     * CASH=1, BANK_TRANSFER=2, E_WALLET=3
     */
    @Column(name = "payment_method", nullable = false)
    private Integer paymentMethod = PaymentMethodConstant.CASH;

    /** Thời điểm thu tiền thực tế (có thể khác createdAt nếu nhập trễ). */
    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;

    /** Mã tham chiếu giao dịch ngân hàng / ví điện tử (nếu có). */
    @Column(name = "transaction_ref", length = 100)
    private String transactionRef;

    /** Ghi chú. */
    @Column(name = "note", length = 500)
    private String note;
}
