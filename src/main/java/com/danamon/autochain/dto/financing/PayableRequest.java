package com.danamon.autochain.dto.financing;

import com.danamon.autochain.constant.financing.FinancingStatus;
import com.danamon.autochain.constant.payment.PaymentType;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.Invoice;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayableRequest {

    private String payment_id;

    private Long amount;

    private Integer tenure;

    private Double monthly_instalment;

    private String payment_method;
}
