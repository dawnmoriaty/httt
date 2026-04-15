package dawn.httt.server.entity;

import dawn.httt.server.constant.CommonStatusConstant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"resource_code", "action_code"})
)
public class PermissionEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "module_code", nullable = false, length = 100)
    private String moduleCode;

    @Column(name = "module_name", nullable = false, length = 150)
    private String moduleName;

    @Column(name = "resource_code", nullable = false, length = 100)
    private String resourceCode;

    @Column(name = "resource_name", nullable = false, length = 150)
    private String resourceName;

    @Column(name = "action_code", nullable = false, length = 50)
    private String actionCode;

    @Column(name = "action_name", nullable = false, length = 100)
    private String actionName;

    @Column(name = "status", nullable = false)
    private Integer status = CommonStatusConstant.STATUS_ACTIVE;
}
