package com.danamon.autochain.dto.Invoice.response;

import com.danamon.autochain.dto.Invoice.ItemList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvoiceGetAllResponse {
    private String company_id;
    private String companyName;
    private String invNumber;
    private Long amount;
    private String dueDate;
    private String status;
    private String processingStatus;
    private List<ItemList> itemList;
}
