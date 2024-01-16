package com.danamon.autochain.repository;

import com.danamon.autochain.entity.InvoiceIssueLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceIssueLogRepository extends JpaRepository<InvoiceIssueLog,String> {
}
