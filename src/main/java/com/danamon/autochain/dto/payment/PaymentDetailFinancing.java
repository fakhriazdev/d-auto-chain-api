package com.danamon.autochain.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentDetailFinancing {
    private String transactionId;
    private String financingId;
    private String tenor;
    private String supplier;
    private Double amount;
    private String paymentMethod;
}
