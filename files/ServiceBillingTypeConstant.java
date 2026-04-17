package dawn.httt.server.constant;

/**
 * Cách tính phí dịch vụ.
 * FIXED      : Phí cố định mỗi tháng (vệ sinh, wifi …)
 * METERED    : Tính theo chỉ số tiêu thụ (điện kWh, nước m³ …)
 */
public final class ServiceBillingTypeConstant {

    public static final int FIXED   = 1;
    public static final int METERED = 2;

    private ServiceBillingTypeConstant() {}
}
