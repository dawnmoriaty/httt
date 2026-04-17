package dawn.httt.server.repository;

import dawn.httt.server.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    List<PaymentEntity> findByInvoiceId(Long invoiceId);

    List<PaymentEntity> findByReceivedByUserId(Long receivedByUserId);

}
