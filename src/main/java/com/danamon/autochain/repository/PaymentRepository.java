package com.danamon.autochain.repository;

import com.danamon.autochain.constant.payment.PaymentStatus;
import com.danamon.autochain.constant.payment.PaymentType;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.Invoice;
import com.danamon.autochain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface    PaymentRepository extends JpaRepository<Payment, String>, JpaSpecificationExecutor<Payment> {
    List<Payment> findAllBySenderIdAndCreatedDateBetween(Company sender, Date startDate, Date endDate);
    List<Payment> findAllByRecipientIdAndCreatedDateBetween(Company recipient, Date startDate, Date endDate);
    List<Payment> findAllByStatusEquals(String status);
    List<Payment> findAllByRecipientIdAndTypeAndStatusInAndAmountBetween(Company recipientId, PaymentType type, List<PaymentStatus> status, Long amount, Long amount2);
    List<Payment> findAllByRecipientIdAndTypeIsNotAndStatusInAndAmountBetween(Company recipientId, PaymentType type, List<PaymentStatus> status, Long amount, Long amount2);
    List<Payment> findAllBySenderIdAndTypeAndStatusInAndAmountBetween(Company senderId, PaymentType type, List<PaymentStatus> status, Long amount, Long amount2);
    List<Payment> findAllBySenderIdAndTypeIsNotAndStatusInAndAmountBetween(Company senderId, PaymentType type, List<PaymentStatus> status, Long amount, Long amount2);
    List<Payment> findAllByInvoiceInAndCreatedDateBetween(List<Invoice> invoices, Date startDate, Date endDate);
    List<Payment> findAllByRecipientIdAndDueDateBetween(Company recipientId, Date dueDate, Date dueDat2);
    List<Payment> findAllBySenderIdAndDueDateBetween(Company senderId, Date dueDate, Date dueDate2);
}
