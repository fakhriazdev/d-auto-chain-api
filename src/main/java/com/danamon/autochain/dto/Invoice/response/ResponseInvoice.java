package com.danamon.autochain.dto.Invoice.response;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseInvoice {
    private String companyName;
    private String invNumber;
    private Long amount;
    private Date dueDate;
    private String Status;
    private String type;
}
