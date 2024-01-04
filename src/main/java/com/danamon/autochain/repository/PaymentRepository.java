package com.danamon.autochain.repository;

import com.danamon.autochain.constant.invoice.Status;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.Invoice;
import com.danamon.autochain.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String>, JpaSpecificationExecutor<Payment> {
//    List<Payment> findAllByInvoiceInAndOutstandingFlagIn(List<Invoice> invoices, List<Status> status);
    Page<Payment> findAllByInvoiceInAndStatusIn(List<Invoice> invoices, List<Status> status, Pageable pageable);
}
