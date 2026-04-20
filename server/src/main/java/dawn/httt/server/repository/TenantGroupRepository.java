package dawn.httt.server.repository;

import dawn.httt.server.entity.TenantGroupEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantGroupRepository extends JpaRepository<TenantGroupEntity, Long> {

    boolean existsByCode(String code);

    Page<TenantGroupEntity> findAllByOrderByIdDesc(Pageable pageable);

    Page<TenantGroupEntity> findAllByRepresentativeUserIdOrderByIdDesc(Long representativeUserId, Pageable pageable);

    @Query("""
            select tg from TenantGroupEntity tg
            where lower(tg.code) like lower(concat('%', :q, '%'))
               or lower(tg.name) like lower(concat('%', :q, '%'))
               or lower(coalesce(tg.note, '')) like lower(concat('%', :q, '%'))
            order by tg.id desc
            """)
    Page<TenantGroupEntity> searchByKeyword(@Param("q") String query, Pageable pageable);

    @Query("""
            select tg from TenantGroupEntity tg
            where tg.representativeUserId = :representativeUserId
              and (
                    lower(tg.code) like lower(concat('%', :q, '%'))
                 or lower(tg.name) like lower(concat('%', :q, '%'))
                 or lower(coalesce(tg.note, '')) like lower(concat('%', :q, '%'))
              )
            order by tg.id desc
            """)
    Page<TenantGroupEntity> searchByRepresentativeAndKeyword(
            @Param("representativeUserId") Long representativeUserId,
            @Param("q") String query,
            Pageable pageable
    );

    @Query("select tg from TenantGroupEntity tg where tg.id in :ids")
    List<TenantGroupEntity> findByIdIn(@Param("ids") List<Long> ids);

    Optional<TenantGroupEntity> findByCode(String code);

    Optional<TenantGroupEntity> findByName(String name);

    List<TenantGroupEntity> findByStatus(Integer status);

    List<TenantGroupEntity> findByRepresentativeUserId(Long representativeUserId);
}
