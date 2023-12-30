package com.danamon.autochain.repository;

import com.danamon.autochain.entity.FinancingReceivable;
import com.danamon.autochain.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FinancingReceivableRepository extends JpaRepository<FinancingReceivable, String>,  JpaSpecificationExecutor<FinancingReceivable> {
}
