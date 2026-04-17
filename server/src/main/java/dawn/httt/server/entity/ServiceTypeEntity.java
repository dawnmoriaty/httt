package dawn.httt.server.entity;

import dawn.httt.server.constant.CommonStatusConstant;
import dawn.httt.server.constant.ServiceBillingTypeConstant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Phân hệ: Dịch vụ — danh mục loại dịch vụ (master data).
 *
 * Tái sử dụng:
 *   - AuditEntity: timestamp tự động.
 *   - status dùng CommonStatusConstant (ACTIVE/INACTIVE) — nhất quán với
 *     toàn bộ hệ thống (UserEntity, RoleEntity …).
 *
 * Ví dụ bản ghi:
 *   code=WIFI,  name=Internet Wifi,    billingType=FIXED,   unitPrice=100000, unit=tháng
 *   code=ELEC,  name=Điện,             billingType=METERED, unitPrice=3500,   unit=kWh
 *   code=WATER, name=Nước,             billingType=METERED, unitPrice=15000,  unit=m³
 *   code=CLEAN, name=Vệ sinh,          billingType=FIXED,   unitPrice=50000,  unit=tháng
 */
@Getter
@Setter
@Entity
@Table(
    name = "service_types",
    indexes = {
        @Index(name = "idx_service_types_code", columnList = "code", unique = true)
    }
)
public class ServiceTypeEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã dịch vụ ngắn, ví dụ: "WIFI", "ELEC", "WATER", "CLEAN". */
    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    /** Tên hiển thị. */
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /**
     * Cách tính phí — dùng ServiceBillingTypeConstant.
     * FIXED=1 (cố định/tháng), METERED=2 (theo chỉ số).
     */
    @Column(name = "billing_type", nullable = false)
    private Integer billingType = ServiceBillingTypeConstant.FIXED;

    /** Đơn giá (VND). Với FIXED: giá/tháng. Với METERED: giá/đơn vị. */
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    /** Đơn vị tính, ví dụ: "tháng", "kWh", "m³". */
    @Column(name = "unit", nullable = false, length = 20)
    private String unit;

    /**
     * Trạng thái — dùng CommonStatusConstant.
     * ACTIVE=1, INACTIVE=2.
     */
    @Column(name = "status", nullable = false)
    private Integer status = CommonStatusConstant.STATUS_ACTIVE;
}
