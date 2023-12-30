package com.danamon.autochain.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchPaymentRequest {
    private Integer page;
    private Integer size;
    private String direction;
    private String status;
    private String type;
}

