package com.danamon.autochain.repository;

import com.danamon.autochain.constant.financing.FinancingStatus;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.FinancingReceivable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinancingReceivableRepository extends JpaRepository<FinancingReceivable, String>,  JpaSpecificationExecutor<FinancingReceivable> {

    @Query(value = "SELECT j.company_name, SUM(c.amount) FROM t_financing_receivable c LEFT JOIN m_company j ON j.company_id = c.company_id GROUP BY j.company_name", nativeQuery = true)
    List<Object[]> findAllAndSumByAmountGroupByCompanyName();
    @Query(value = "SELECT j.company_name, AVG(c.amount) FROM t_financing_receivable c LEFT JOIN m_company j ON j.company_id = c.company_id GROUP BY j.company_name", nativeQuery = true)
    List<Object[]> findAllByAverageSumGroupByCompanyName();
    @Query(value = "SELECT j.company_name, COUNT(*) FROM t_financing_receivable c LEFT JOIN m_company j ON j.company_id = c.company_id WHERE c.status = :status GROUP BY j.company_name", nativeQuery = true)
    List<Object[]> findAllByStatusGroupByCompanyName(@Param("status") String status);

//    =============================================================
    Long countByStatus(FinancingStatus status);
    @Query("select sum(fr.amount) from FinancingReceivable fr where fr.company = ?1 and fr.status = ?2 or fr.status = ?3")
    Optional<Long> findAllByCompanyAndStatusIsAndStatusIs(Company company, FinancingStatus status, FinancingStatus status2);
}
