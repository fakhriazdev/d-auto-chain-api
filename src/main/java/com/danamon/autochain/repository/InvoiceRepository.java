package com.danamon.autochain.repository;

import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, String>, JpaSpecificationExecutor<Invoice> {
    Optional<Invoice> findInvoiceByRecipientIdAndInvoiceId(Company recipientId, String invoiceId);
    Optional<Invoice> findInvoiceBySenderIdAndInvoiceId(Company recipientId, String invoiceId);
}
