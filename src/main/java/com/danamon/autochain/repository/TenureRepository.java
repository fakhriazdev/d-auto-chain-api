package com.danamon.autochain.repository;

import com.danamon.autochain.constant.TenureStatus;
import com.danamon.autochain.entity.FinancingPayable;
import com.danamon.autochain.entity.Payment;
import com.danamon.autochain.entity.Tenure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenureRepository extends JpaRepository<Tenure,String> {
    List<Tenure> findAllByfinancingPayableId(FinancingPayable financingPayable);

    List<Tenure> findAllByFinancingPayableIdAndStatusIsOrderByDueDateAsc(FinancingPayable financingPayableId, TenureStatus status);

    Optional<Tenure> findByFinancingPayableIdAndStatusIs(FinancingPayable financingPayableId, TenureStatus status);
}
