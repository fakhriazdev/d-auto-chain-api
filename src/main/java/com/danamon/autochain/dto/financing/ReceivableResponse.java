package com.danamon.autochain.dto.financing;

import com.danamon.autochain.constant.FinancingStatus;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.Invoice;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

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
