package com.danamon.autochain.repository;

import com.danamon.autochain.constant.financing.FinancingStatus;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.FinancingPayable;
import com.danamon.autochain.entity.Payment;
import com.danamon.autochain.service.impl.FinancingServiceImpl;
import org.hibernate.query.spi.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FinancingPayableRepository extends JpaRepository<FinancingPayable, String>, JpaSpecificationExecutor<FinancingPayable> {
    Long countByStatus(FinancingStatus status);

    Optional<FinancingPayable> findByPayment(Payment payment);

    List<FinancingPayable> findAllByCompanyAndStatusIsOrStatusIs(Company company, FinancingStatus status, FinancingStatus status2);
    List<FinancingPayable> findAllByCompanyAndStatusIs(Company company, FinancingStatus status2);

    //    =============================================================
    @Query(value = "SELECT j.company_name, SUM(c.amount) FROM t_financing_payable c LEFT JOIN m_company j ON j.company_id = c.company_id GROUP BY j.company_name", nativeQuery = true)
    List<Object[]> findAllAndSumByAmountGroupByCompanyName();
    @Query(value = "SELECT j.company_name, AVG(c.amount) FROM t_financing_receivable c LEFT JOIN m_company j ON j.company_id = c.company_id GROUP BY j.company_name", nativeQuery = true)
    List<Object[]> findAllByAverageSumGroupByCompanyName();
    @Query(value = "SELECT j.company_name, COUNT(*) FROM t_financing_receivable c LEFT JOIN m_company j ON j.company_id = c.company_id WHERE c.status = :status GROUP BY j.company_name", nativeQuery = true)
    List<Object[]> findAllByStatusGroupByCompanyName(@Param("status") String status);
}