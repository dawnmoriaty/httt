package dawn.httt.server.entity;

import dawn.httt.server.constant.AssetConditionConstant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Phân hệ: Cơ sở vật chất — thiết bị / tài sản gắn với phòng.
 * Ví dụ: máy lạnh, camera, bình nóng lạnh, khóa thông minh.
 *
 * Tái sử dụng:
 *   - AuditEntity: timestamp tự động.
 *   - FK room_id → RoomEntity: tận dụng index idx_rooms_code để join nhanh.
 */
@Getter
@Setter
@Entity
@Table(
    name = "assets",
    indexes = {
        @Index(name = "idx_assets_room_id", columnList = "room_id"),
        @Index(name = "idx_assets_asset_type", columnList = "asset_type")
    }
)
public class AssetEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * FK → rooms.id.
     * Lưu raw id thay vì @ManyToOne để nhất quán với pattern SubscriptionEntity
     * (ownerUserId). Nếu sau này cần join, dùng JPQL với id.
     */
    @Column(name = "room_id", nullable = false)
    private Long roomId;

    /** Tên tài sản, ví dụ: "Máy lạnh Daikin 1.5HP". */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * Loại tài sản (mã số tự do, không cần constant vì có thể mở rộng).
     * Ví dụ: 1=Điều hoà, 2=Camera, 3=Bình nóng lạnh, 4=Khoá, 5=Khác.
     */
    @Column(name = "asset_type", nullable = false)
    private Integer assetType;

    /** Số serial / mã vật tư để phân biệt từng cái. */
    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    /**
     * Tình trạng vật lý — dùng AssetConditionConstant.
     * GOOD=1, WORN=2, BROKEN=3, DISPOSED=4
     */
    @Column(name = "condition_status", nullable = false)
    private Integer conditionStatus = AssetConditionConstant.GOOD;

    /** Ghi chú (vị trí lắp đặt, lịch bảo trì …). */
    @Column(name = "note", length = 500)
    private String note;
}
