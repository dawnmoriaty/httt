package dawn.httt.server.repository;

import dawn.httt.server.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long> {

    List<InvoiceEntity> findBySubscriptionId(Long subscriptionId);

    List<InvoiceEntity> findByUserId(Long userId);

    List<InvoiceEntity> findByRoomId(Long roomId);

    List<InvoiceEntity> findByStatus(Integer status);

    Optional<InvoiceEntity> findBySubscriptionIdAndInvoiceNumber(Long subscriptionId, String invoiceNumber);

    List<InvoiceEntity> findBySubscriptionIdAndStatusIn(Long subscriptionId, List<Integer> statuses);

}
