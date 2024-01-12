package com.danamon.autochain.repository;

import com.danamon.autochain.constant.financing.FinancingStatus;
import com.danamon.autochain.entity.FinancingPayable;
import com.danamon.autochain.service.impl.FinancingServiceImpl;
import org.hibernate.query.spi.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FinancingPayableRepository extends JpaRepository<FinancingPayable, String>, JpaSpecificationExecutor<FinancingPayable> {
    Long countByStatus(FinancingStatus status);

}