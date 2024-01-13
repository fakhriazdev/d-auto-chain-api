package com.danamon.autochain.repository;

import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Date;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, String>, JpaSpecificationExecutor<Payment> {
    List<Payment> findAllBySenderIdAndDueDateBetween(Company sender, Date startDate, Date endDate);
    List<Payment> findAllByRecipientIdAndDueDateBetween(Company recipient, Date startDate, Date endDate);
    List<Payment> findAllByStatusEqualsIgnoreCase(String status);
}
