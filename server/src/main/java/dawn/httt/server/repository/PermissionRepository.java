package dawn.httt.server.repository;

import dawn.httt.server.entity.PermissionEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {

    Optional<PermissionEntity> findByResourceCodeAndActionCode(String resourceCode, String actionCode);

    List<PermissionEntity> findAllByIdIn(Collection<Long> ids);

    Page<PermissionEntity> findAllByOrderByModuleNameAscResourceNameAscActionNameAsc(Pageable pageable);
}
