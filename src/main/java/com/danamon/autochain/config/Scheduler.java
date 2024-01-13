package com.danamon.autochain.config;

import com.danamon.autochain.constant.payment.PaymentStatus;
import com.danamon.autochain.entity.Payment;
import com.danamon.autochain.repository.CompanyRepository;
import com.danamon.autochain.repository.PaymentRepository;
import com.danamon.autochain.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Scheduler {
    private PaymentRepository paymentRepository;

    @Scheduled(cron = "0 30 0 * * *")
    public void changePaymentStatus() {
        List<Payment> payments = paymentRepository.findAllByStatusEquals("UNPAID");

        LocalDate currentDate = LocalDate.now();

        payments.forEach(payment -> {
            LocalDate dueDate = payment.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            if (currentDate.isAfter(dueDate)) {
                payment.setStatus(PaymentStatus.LATE_UNPAID);
                paymentRepository.save(payment);
            }
        });
    }
}
