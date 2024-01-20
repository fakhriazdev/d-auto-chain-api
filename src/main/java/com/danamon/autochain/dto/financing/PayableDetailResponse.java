package com.danamon.autochain.dto.financing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Builder
@Data
@AllArgsConstructor
public class PayableDetailResponse {
    String financing_id;
    String payment_id;
    String created_date;
    Map<String,String> recipient;
    Map<String,String> sender;
    Long total_amount;
    Integer tenure;
    Double amount_instalment;
    List<TenureDetailResponse> tenure_list_detail;
    String status;
}
