package com.danamon.autochain.dto.Invoice.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestInvoiceStatus {
    private String invNumber;
    private String processingType;
    private String reasonType;
    private String reason;
}
