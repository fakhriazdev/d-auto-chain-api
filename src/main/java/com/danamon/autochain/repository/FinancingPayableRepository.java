package com.danamon.autochain.repository;

import com.danamon.autochain.constant.financing.FinancingStatus;
import com.danamon.autochain.entity.FinancingPayable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FinancingPayableRepository extends JpaRepository<FinancingPayable, String>, JpaSpecificationExecutor<FinancingPayable> {
    Long countByStatus(FinancingStatus status);
}
