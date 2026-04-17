package dawn.httt.server.constant;

/**
 * Trạng thái hoá đơn hàng tháng.
 */
public final class InvoiceStatusConstant {

    /** Hoá đơn mới tạo, chờ thanh toán */
    public static final int UNPAID       = 1;

    /** Đã thanh toán đủ */
    public static final int PAID         = 2;

    /** Đã quá hạn thanh toán */
    public static final int OVERDUE      = 3;

    /** Hoá đơn đã huỷ (hợp đồng chấm dứt sớm …) */
    public static final int CANCELLED    = 4;

    private InvoiceStatusConstant() {}
}
