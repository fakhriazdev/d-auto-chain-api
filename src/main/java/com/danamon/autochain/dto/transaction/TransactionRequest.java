package com.danamon.autochain.dto.transaction;

import com.danamon.autochain.constant.financing.FinancingType;
import com.danamon.autochain.constant.payment.PaymentStatus;
import com.danamon.autochain.entity.Company;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
@AllArgsConstructor
public class TransactionRequest {
    private Company recipientId;

    private Long amount;

    private PaymentStatus paymentStatus;

    private FinancingType financingType;

    private Date createdDate;
}
