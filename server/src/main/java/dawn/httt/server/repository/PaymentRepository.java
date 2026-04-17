package dawn.httt.server.repository;

import dawn.httt.server.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    List<PaymentEntity> findByInvoiceId(Long invoiceId);

    List<PaymentEntity> findByUserId(Long userId);

    List<PaymentEntity> findByStatus(Integer status);

    List<PaymentEntity> findByPaymentDate(LocalDate paymentDate);

    List<PaymentEntity> findBySubscriptionId(Long subscriptionId);

}
