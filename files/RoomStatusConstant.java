package dawn.httt.server.constant;

/**
 * Trạng thái phòng.
 * Dùng Integer thay vì Enum để nhất quán với CommonStatusConstant đã có.
 */
public final class RoomStatusConstant {

    /** Phòng trống, sẵn sàng cho thuê */
    public static final int VACANT = 1;

    /** Phòng đang có hợp đồng thuê */
    public static final int OCCUPIED = 2;

    /** Phòng đang bảo trì, không cho thuê */
    public static final int MAINTENANCE = 3;

    private RoomStatusConstant() {}
}
