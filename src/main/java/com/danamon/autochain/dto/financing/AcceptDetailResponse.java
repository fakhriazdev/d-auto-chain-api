package com.danamon.autochain.dto.financing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AcceptDetailResponse {
    String company_name;
    Long amountPaid;
    String financingType;
}
