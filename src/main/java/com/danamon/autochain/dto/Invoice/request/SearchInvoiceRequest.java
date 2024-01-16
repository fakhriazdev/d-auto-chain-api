package com.danamon.autochain.dto.Invoice.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchInvoiceRequest {
    private Integer page;
    private Integer size;
    private String direction;
    private String status;
    private String type;
}

