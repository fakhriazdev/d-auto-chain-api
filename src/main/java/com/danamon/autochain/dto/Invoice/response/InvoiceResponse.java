package com.danamon.autochain.dto.Invoice.response;

import com.danamon.autochain.dto.Invoice.ItemList;
import com.danamon.autochain.entity.InvoiceIssueLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvoiceResponse {
    private String company_id;
    private String companyName;
    private String invNumber;
    private Long amount;
    private Date dueDate;
    private String status;
    private List<ItemList> itemList;
    private InvoiceIssueLog issue;
}
