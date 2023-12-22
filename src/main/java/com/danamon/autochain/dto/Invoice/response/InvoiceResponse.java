package com.danamon.autochain.dto.Invoice.response;

import com.danamon.autochain.entity.ItemList;
import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvoiceResponse {
    private String companyName;
    private String invNumber;
    private Long amount;
    private Date dueDate;
    private String Status;
    private String type;
    private List<ItemList> itemList;
}
