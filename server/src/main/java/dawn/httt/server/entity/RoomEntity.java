package dawn.httt.server.entity;

import dawn.httt.server.constant.RoomStatusConstant;
import dawn.httt.server.constant.RoomTypeConstant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;

/**
 * Phân hệ: Cơ sở vật chất — đơn vị phòng cho thuê.
 *
 * Tái sử dụng: extends AuditEntity (createdAt / updatedAt tự động).
 * Liên kết ngược: ContractEntity, AssetEntity đều FK về room_id.
 */
@Getter
@Setter
@Entity
@Table(
    name = "rooms",
    indexes = {
        @Index(name = "idx_rooms_code", columnList = "code", unique = true),
        @Index(name = "idx_rooms_status", columnList = "status"),
        @Index(name = "idx_rooms_active_contract_id", columnList = "active_contract_id")
    }
)
public class RoomEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã phòng, ví dụ: "P101", "P202". Unique, dùng để tra cứu nhanh. */
    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    /** Tên hiển thị, ví dụ: "Phòng 101 – Tầng 1". */
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /** Tầng của phòng. */
    @Column(name = "floor", nullable = false)
    private Integer floor;

    /**
     * Loại phòng — dùng RoomTypeConstant.
     * STANDARD=1, STUDIO=2, DELUXE=3, PENTHOUSE=4
     */
    @Column(name = "room_type", nullable = false)
    private Integer roomType = RoomTypeConstant.STANDARD;

    /** Diện tích (m²). */
    @Column(name = "area_m2", precision = 6, scale = 2)
    private BigDecimal areaM2;

    @Column(name = "max_occupants")
    private Integer maxOccupants;

    /**
     * Giá thuê cơ bản (VND/tháng).
     * Hợp đồng có thể ghi đè giá này khi đàm phán.
     */
    @Column(name = "base_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal basePrice;

    /**
     * Trạng thái phòng — dùng RoomStatusConstant.
     * VACANT=1, OCCUPIED=2, MAINTENANCE=3
     * Cập nhật tự động khi tạo / kết thúc hợp đồng.
     */
    @Column(name = "status", nullable = false)
    private Integer status = RoomStatusConstant.VACANT;

    /** Ghi chú tự do (hướng phòng, tiện ích đặc biệt …). */
    @Column(name = "note", length = 1000)
    private String note;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_contract_id")
    private ContractEntity activeContract;
}
