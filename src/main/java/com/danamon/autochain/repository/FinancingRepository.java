package com.danamon.autochain.repository;

import com.danamon.autochain.entity.FinancingReceivable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancingRepository  extends JpaRepository<FinancingReceivable, String> {
}
