package dawn.httt.server.repository;

import dawn.httt.server.entity.RoleEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByCode(String code);

    boolean existsByCode(String code);

    @EntityGraph(attributePaths = {"permissions"})
    Optional<RoleEntity> findWithPermissionsById(Long id);

    Page<RoleEntity> findAllByOrderByNameAsc(Pageable pageable);

    @Query("""
            select r from RoleEntity r
            where lower(r.code) like lower(concat('%', :q, '%'))
               or lower(r.name) like lower(concat('%', :q, '%'))
               or lower(coalesce(r.description, '')) like lower(concat('%', :q, '%'))
            order by r.name asc
            """)
    Page<RoleEntity> searchByKeyword(@Param("q") String query, Pageable pageable);

    @EntityGraph(attributePaths = {"permissions"})
    @Query("select r from RoleEntity r where r.id in :ids")
    List<RoleEntity> findWithPermissionsByIdIn(@Param("ids") List<Long> ids);
}
