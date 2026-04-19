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

    Page<UserEntity> findAllByOrderByIdAsc(Pageable pageable);

    @Query("""
            select u from UserEntity u
            where lower(u.username) like lower(concat('%', :q, '%'))
               or lower(u.fullName) like lower(concat('%', :q, '%'))
               or lower(u.email) like lower(concat('%', :q, '%'))
            order by u.id desc
            """)
    Page<UserEntity> searchByKeyword(@Param("q") String query, Pageable pageable);

    @EntityGraph(attributePaths = {"roles"})
    @Query("select u from UserEntity u where u.id in :ids")
    List<UserEntity> findWithRolesByIdIn(@Param("ids") List<Long> ids);

    @Query("select distinct u.id from UserEntity u join u.roles r where r.id = :roleId")
    List<Long> findDistinctIdsByRoleId(@Param("roleId") Long roleId);
}
