package com.danamon.autochain.dto.Invoice.response;

import com.danamon.autochain.dto.company.CompanyResponse;
import com.danamon.autochain.entity.ItemList;
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
public class InvoiceDetailResponse {
    private String invoiceId;
    private Date date;
    private Date dueDate;
    private CompanyResponse companyRecipient;
    private CompanyResponse companyFrom;
    private List<ItemList> itemList;
}
