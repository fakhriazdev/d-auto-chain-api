package com.danamon.autochain.repository;

import com.danamon.autochain.constant.financing.FinancingStatus;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.FinancingReceivable;
import com.danamon.autochain.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinancingReceivableRepository extends JpaRepository<FinancingReceivable, String>,  JpaSpecificationExecutor<FinancingReceivable> {
    Long countByStatus(FinancingStatus status);
    @Query("select sum(fr.amount) from FinancingReceivable fr where fr.company = ?1 and fr.status = ?2 or fr.status = ?3")
    Optional<Long> findAllByCompanyAndStatusIsAndStatusIs(Company company, FinancingStatus status, FinancingStatus status2);
}
