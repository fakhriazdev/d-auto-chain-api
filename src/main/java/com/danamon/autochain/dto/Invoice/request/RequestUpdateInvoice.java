package com.danamon.autochain.dto.Invoice.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestUpdateInvoice {
    private String id;
    @NotBlank(message = "Due Date is require")
    private Date dueDate;
    @NotNull(message = "Amount is require")
    @Min(value = 1, message = "amount must greater then 0")
    private Long amount;
    private Date invDate;
    @NotNull(message = "Item List is require")
    private String itemList;
}
