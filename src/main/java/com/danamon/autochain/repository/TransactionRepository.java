package com.danamon.autochain.repository;

import com.danamon.autochain.entity.TransactionDanamon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionDanamon, String> {
}
