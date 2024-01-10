package com.danamon.autochain.dto.financing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AcceptResponse {
    String payment_type;
    AcceptDetailResponse buyyer;
    AcceptDetailResponse seller;
}
