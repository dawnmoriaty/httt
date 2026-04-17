package dawn.httt.server.constant;

/**
 * Phương thức thanh toán được ghi nhận khi thu tiền.
 */
public final class PaymentMethodConstant {

    public static final int CASH         = 1;   // Tiền mặt
    public static final int BANK_TRANSFER = 2;  // Chuyển khoản
    public static final int E_WALLET     = 3;   // Ví điện tử (Momo, ZaloPay …)

    private PaymentMethodConstant() {}
}
