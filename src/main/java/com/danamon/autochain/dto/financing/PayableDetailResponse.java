package com.danamon.autochain.dto.financing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Builder
@Data
@AllArgsConstructor
public class PayableDetailResponse {
    String financing_id;
    String invoice_number;
    Date created_date;
    Map<String,String> recipient;
    Map<String,String> sender;
    Long total_amount;
    Integer tenure_instalment;
    List<TenureDetailResponse> tenure_list;
}
