package com.danamon.autochain.dto.financing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ReceivableResponse {
    private String financing_id;

    private String invoice_id;

    private String company_id;

    private String status;

    private Double fee;

    private Double total;

    private Long amount;

    private String disbursement_date;

    private String  financingType;
}
