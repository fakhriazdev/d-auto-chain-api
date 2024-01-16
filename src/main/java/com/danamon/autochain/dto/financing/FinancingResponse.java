package com.danamon.autochain.dto.financing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
@AllArgsConstructor
public class FinancingResponse {
    String financing_id;
    Date date;
    String invoice_number;
    Long Amount;
    String company_name;
    String status;
}
