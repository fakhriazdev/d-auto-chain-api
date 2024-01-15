package com.danamon.autochain.dto.financing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder
public class ReceivableDetailResponse {
    String invoice_number;
    Date created_date;
    Map<String,String> recipient;
    Map<String,String> sender;
    String type;
    Long amount;
    Double Fee;
    Double total;
}
