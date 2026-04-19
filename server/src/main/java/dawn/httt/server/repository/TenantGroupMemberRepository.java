package dawn.httt.server.repository;

import dawn.httt.server.constant.TenantMemberRoleConstant;
import dawn.httt.server.entity.TenantGroupMemberEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantGroupMemberRepository extends JpaRepository<TenantGroupMemberEntity, Long> {

    @EntityGraph(attributePaths = {"user"})
    Page<TenantGroupMemberEntity> findByTenantGroup_IdOrderByIdAsc(Long tenantGroupId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    @Query("""
            select m from TenantGroupMemberEntity m
            where m.tenantGroup.id = :tenantGroupId
              and (
                    lower(m.user.username) like lower(concat('%', :q, '%'))
                 or lower(m.user.fullName) like lower(concat('%', :q, '%'))
                 or lower(m.user.email) like lower(concat('%', :q, '%'))
                 or lower(coalesce(m.idCardNumber, '')) like lower(concat('%', :q, '%'))
              )
            order by m.id asc
            """)
    Page<TenantGroupMemberEntity> searchByTenantGroupAndKeyword(
            @Param("tenantGroupId") Long tenantGroupId,
            @Param("q") String query,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"user"})
    Optional<TenantGroupMemberEntity> findByIdAndTenantGroup_Id(Long id, Long tenantGroupId);

    @EntityGraph(attributePaths = {"user"})
    Optional<TenantGroupMemberEntity> findByTenantGroup_IdAndUser_Id(Long tenantGroupId, Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<TenantGroupMemberEntity> findByTenantGroup_IdAndMemberRole(Long tenantGroupId, Integer memberRole);

    boolean existsByTenantGroup_IdAndUser_Id(Long tenantGroupId, Long userId);

    boolean existsByTenantGroup_Id(Long tenantGroupId);

    long countByTenantGroup_Id(Long tenantGroupId);

    void deleteByTenantGroup_Id(Long tenantGroupId);

    default List<TenantGroupMemberEntity> findRepresentatives(Long tenantGroupId) {
        return findByTenantGroup_IdAndMemberRole(tenantGroupId, TenantMemberRoleConstant.REPRESENTATIVE);
    }
}
