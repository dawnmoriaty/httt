package dawn.httt.server.repository;

import dawn.httt.server.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long> {

    List<InvoiceEntity> findByContractId(Long contractId);

    List<InvoiceEntity> findByContract_Id(Long contractId);

    List<InvoiceEntity> findByStatus(Integer status);

    List<InvoiceEntity> findByStatusIn(List<Integer> statuses);

    List<InvoiceEntity> findByBillingYearAndBillingMonth(Integer billingYear, Integer billingMonth);

}
