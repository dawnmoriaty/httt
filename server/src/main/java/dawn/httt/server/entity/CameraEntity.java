package dawn.httt.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cameras", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"room_id", "code"}),
    @UniqueConstraint(columnNames = {"ip_address"})
})
public class CameraEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomEntity room;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "ip_address", nullable = false, length = 50)
    private String ipAddress;

    @Column(name = "rtsp_url", length = 500)
    private String rtspUrl;

    @Column(name = "status", nullable = false)
    private Integer status = 1; // 1=ACTIVE, 2=OFFLINE, 3=MAINTENANCE, 4=INACTIVE

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "installation_date")
    private LocalDate installationDate;
}
