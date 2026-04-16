package dawn.httt.server.repository;

import dawn.httt.server.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByRoles_Id(Long roleId);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<UserEntity> findWithRolesByUsername(String username);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<UserEntity> findWithRolesById(Long id);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<UserEntity> findAuthSnapshotByUsername(String username);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<UserEntity> findAuthSnapshotById(Long id);

    @EntityGraph(attributePaths = {"roles"})
    Page<UserEntity> findAllByOrderByIdAsc(Pageable pageable);

    @Query("select distinct u.id from UserEntity u join u.roles r where r.id = :roleId")
    List<Long> findDistinctIdsByRoleId(@Param("roleId") Long roleId);
}
