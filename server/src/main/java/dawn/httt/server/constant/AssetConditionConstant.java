package dawn.httt.server.constant;

/**
 * Tình trạng vật lý của tài sản (thiết bị) trong phòng.
 */
public final class AssetConditionConstant {

    public static final int GOOD      = 1;   // Tốt
    public static final int WORN      = 2;   // Đã cũ nhưng còn dùng được
    public static final int BROKEN    = 3;   // Hỏng, cần sửa
    public static final int DISPOSED  = 4;   // Đã thanh lý

    private AssetConditionConstant() {}
}
