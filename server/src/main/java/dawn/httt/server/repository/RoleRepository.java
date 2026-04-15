package dawn.httt.server.repository;

import dawn.httt.server.entity.RoleEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByCode(String code);

    boolean existsByCode(String code);

    @EntityGraph(attributePaths = {"permissions"})
    Optional<RoleEntity> findWithPermissionsById(Long id);

    @EntityGraph(attributePaths = {"permissions"})
    List<RoleEntity> findAllByOrderByNameAsc();
}
