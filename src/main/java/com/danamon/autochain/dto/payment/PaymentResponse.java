package com.danamon.autochain.dto.payment;

import com.danamon.autochain.dto.Invoice.response.InvoiceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentResponse {
    private String payment_id;
    private InvoiceResponse invoice;
    private Long amount;
    private String type;
    private String dueDate;
    private String paidDate;
    private String method;
    private String status;
    private String recepient;
}
