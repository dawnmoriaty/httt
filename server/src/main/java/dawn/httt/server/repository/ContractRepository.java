package dawn.httt.server.repository;

import dawn.httt.server.entity.ContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<ContractEntity, Long> {

    List<ContractEntity> findByRoomId(Long roomId);

    List<ContractEntity> findByRoom_Id(Long roomId);

    List<ContractEntity> findByTenantGroupId(Long tenantGroupId);

    List<ContractEntity> findByTenantGroup_Id(Long tenantGroupId);

    List<ContractEntity> findByStatus(Integer status);

    List<ContractEntity> findByTenantGroupIdAndStatus(Long tenantGroupId, Integer status);

    List<ContractEntity> findByRoomIdAndStatus(Long roomId, Integer status);

}
