package com.danamon.autochain.dto.financing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class AcceptResponse {
    String payment_type;
    AcceptDetailResponse buyyer;
    AcceptDetailResponse seller;
}
