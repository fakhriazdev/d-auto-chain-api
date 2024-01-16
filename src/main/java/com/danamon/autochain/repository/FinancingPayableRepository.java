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

import java.util.List;
import java.util.Optional;

public interface FinancingPayableRepository extends JpaRepository<FinancingPayable, String>, JpaSpecificationExecutor<FinancingPayable> {
    Long countByStatus(FinancingStatus status);

    Optional<FinancingPayable> findByPayment(Payment payment);

    List<FinancingPayable> findAllByCompanyAndStatusIsOrStatusIs(Company company, FinancingStatus status, FinancingStatus status2);
    List<FinancingPayable> findAllByCompanyAndStatusIs(Company company, FinancingStatus status2);

}