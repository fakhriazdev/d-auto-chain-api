package com.danamon.autochain.repository;

import com.danamon.autochain.constant.invoice.Status;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.Invoice;
import com.danamon.autochain.entity.Payment;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String>, JpaSpecificationExecutor<Payment> {
    List<Payment> findAllByInvoiceInAndOutstandingFlagIn(List<Invoice> invoices, List<Status> status);
}
