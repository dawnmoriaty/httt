package dawn.httt.server.repository;

import dawn.httt.server.entity.ContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<ContractEntity, Long> {

    List<ContractEntity> findBySubscriptionId(Long subscriptionId);

    List<ContractEntity> findByRoomId(Long roomId);

    List<ContractEntity> findByUserId(Long userId);

    List<ContractEntity> findByStatus(Integer status);

    Optional<ContractEntity> findBySubscriptionIdAndContractNumber(Long subscriptionId, String contractNumber);

    List<ContractEntity> findBySubscriptionIdAndStatus(Long subscriptionId, Integer status);

}
