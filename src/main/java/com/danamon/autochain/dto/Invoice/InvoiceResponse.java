package com.danamon.autochain.dto.Invoice;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Builder
@Data
public class InvoiceResponse {
    private String invoice_id;
    private String company;
    private Date dueDate;
    private String status;
    private Integer amount;
    private String type;
}