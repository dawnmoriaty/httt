package dawn.httt.server.repository;

import dawn.httt.server.entity.TenantGroupEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantGroupRepository extends JpaRepository<TenantGroupEntity, Long> {

    boolean existsByCode(String code);

    @EntityGraph(attributePaths = {"leader"})
    Page<TenantGroupEntity> findAllByOrderByIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"leader"})
    Page<TenantGroupEntity> findAllByRepresentativeUserIdOrderByIdDesc(Long representativeUserId, Pageable pageable);

    @EntityGraph(attributePaths = {"leader"})
    Page<TenantGroupEntity> findAllByLeader_Subscription_IdOrderByIdDesc(Long subscriptionId, Pageable pageable);

    @EntityGraph(attributePaths = {"leader"})
    Optional<TenantGroupEntity> findWithLeaderById(Long id);

    Optional<TenantGroupEntity> findByCode(String code);

    Optional<TenantGroupEntity> findByName(String name);

    List<TenantGroupEntity> findByStatus(Integer status);

    List<TenantGroupEntity> findByRepresentativeUserId(Long representativeUserId);
}
