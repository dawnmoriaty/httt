package dawn.httt.server.repository;

import dawn.httt.server.entity.SubscriptionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {

    Page<SubscriptionEntity> findAllByOrderByIdDesc(Pageable pageable);

    Page<SubscriptionEntity> findAllByOwnerUserIdOrderByIdDesc(Long ownerUserId, Pageable pageable);

    @Query("""
            select s from SubscriptionEntity s
            where lower(s.title) like lower(concat('%', :q, '%'))
               or lower(coalesce(s.description, '')) like lower(concat('%', :q, '%'))
            order by s.id desc
            """)
    Page<SubscriptionEntity> searchByKeyword(@Param("q") String query, Pageable pageable);

    @Query("""
            select s from SubscriptionEntity s
            where s.ownerUserId = :ownerUserId
              and (
                    lower(s.title) like lower(concat('%', :q, '%'))
                 or lower(coalesce(s.description, '')) like lower(concat('%', :q, '%'))
              )
            order by s.id desc
            """)
    Page<SubscriptionEntity> searchByOwnerAndKeyword(@Param("ownerUserId") Long ownerUserId, @Param("q") String query, Pageable pageable);

    Optional<SubscriptionEntity> findByIdAndOwnerUserId(Long id, Long ownerUserId);
}
