package com.danamon.autochain.repository;

import com.danamon.autochain.constant.invoice.InvoiceStatus;
import com.danamon.autochain.constant.invoice.ProcessingStatusType;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String>, JpaSpecificationExecutor<Invoice> {
    Optional<Invoice> findInvoiceByRecipientIdAndInvoiceId(Company recipientId, String invoiceId);
    Optional<Invoice> findInvoiceBySenderIdAndInvoiceId(Company recipientId, String invoiceId);
    List<Invoice> findAllByRecipientId(Company recipient);
    List<Invoice> findAllByRecipientIdAndStatusOrStatus(Company recipientId, InvoiceStatus status, InvoiceStatus status2);
    List<Invoice> findAllBySenderIdAndStatusOrStatus(Company senderId, InvoiceStatus status, InvoiceStatus status2);
    List<Invoice> findAllBySenderId(Company sender);
    List<Invoice> findAllBySenderIdInAndRecipientId(List<Company> senders, Company recipient);
    List<Invoice> findAllByRecipientIdInAndSenderId(List<Company> recipients, Company sender);
    List<Invoice> findAllBySenderIdAndStatusIn(Company company, List<InvoiceStatus> statuses);
}
