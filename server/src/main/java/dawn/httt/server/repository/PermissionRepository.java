package dawn.httt.server.repository;

import dawn.httt.server.entity.PermissionEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {

    Optional<PermissionEntity> findByResourceCodeAndActionCode(String resourceCode, String actionCode);

    List<PermissionEntity> findAllByIdIn(Collection<Long> ids);

    Page<PermissionEntity> findAllByOrderByModuleNameAscResourceNameAscActionNameAsc(Pageable pageable);

    @Query("""
            select p from PermissionEntity p
            where lower(p.moduleName) like lower(concat('%', :q, '%'))
               or lower(p.moduleCode) like lower(concat('%', :q, '%'))
               or lower(p.resourceName) like lower(concat('%', :q, '%'))
               or lower(p.resourceCode) like lower(concat('%', :q, '%'))
               or lower(p.actionCode) like lower(concat('%', :q, '%'))
               or lower(p.actionName) like lower(concat('%', :q, '%'))
            order by p.moduleName asc, p.resourceName asc, p.actionName asc
            """)
    Page<PermissionEntity> searchByKeyword(@Param("q") String query, Pageable pageable);
}
