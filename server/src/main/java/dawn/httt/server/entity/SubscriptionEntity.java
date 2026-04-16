package dawn.httt.server.entity;

import dawn.httt.server.constant.CommonStatusConstant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "subscriptions", indexes = {
        @Index(name = "idx_subscriptions_owner_user_id", columnList = "owner_user_id")
})
public class SubscriptionEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "status", nullable = false)
    private Integer status = CommonStatusConstant.STATUS_ACTIVE;

    @Column(name = "owner_user_id")
    private Long ownerUserId;
}
