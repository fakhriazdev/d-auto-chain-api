package com.danamon.autochain.dto.payment;

import com.danamon.autochain.constant.payment.PaymentMethod;
import com.danamon.autochain.constant.payment.PaymentStatus;
import com.danamon.autochain.constant.payment.PaymentType;
import com.danamon.autochain.constant.invoice.Status;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.FinancingPayable;
import com.danamon.autochain.entity.Invoice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
@Builder
public class CreatePaymentRequest {
    private Invoice invoice;

    private FinancingPayable financingPayable;

    private Company recipientId;

    private Company senderId;

    private Long amount;

    private PaymentType type;

    private Date dueDate;

    private Date paidDate;

    private PaymentMethod method;

    private PaymentStatus status;
}
