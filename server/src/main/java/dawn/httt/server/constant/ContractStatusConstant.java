package dawn.httt.server.constant;

/**
 * Trạng thái vòng đời của một hợp đồng thuê.
 */
public final class ContractStatusConstant {

    /** Hợp đồng nháp, chưa có hiệu lực */
    public static final int DRAFT      = 1;

    /** Hợp đồng đang có hiệu lực */
    public static final int ACTIVE     = 2;

    /** Hợp đồng đã hết hạn tự nhiên */
    public static final int EXPIRED    = 3;

    /** Hợp đồng bị chấm dứt sớm */
    public static final int TERMINATED = 4;

    private ContractStatusConstant() {}
}
