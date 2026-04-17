package dawn.httt.server.repository;

import dawn.httt.server.entity.InvoiceItemEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItemEntity, Long> {

    List<InvoiceItemEntity> findByInvoiceId(Long invoiceId);

    List<InvoiceItemEntity> findByServiceUsageId(Long serviceUsageId);
}
