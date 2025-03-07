package com.danamon.autochain.dto.Invoice.response;

import com.danamon.autochain.dto.Invoice.ItemList;
import com.danamon.autochain.dto.company.CompanyResponse;
import com.danamon.autochain.entity.InvoiceIssueLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvoiceDetailResponse {
    private String invoiceId;
    private Date date;
    private Date dueDate;
    private CompanyResponse companyRecipient;
    private CompanyResponse companyFrom;
    private String processingStatus;
    private Long amount;
    private String type;
    private List<ItemList> itemList;
    private String issue;
    private String reason;
}
