package com.danamon.autochain.dto.Invoice.request;

import com.google.gson.JsonObject;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class RequestInvoice {
    @NotBlank(message = "Recipient is require")
    private String recipientId; // company id
    @NotBlank(message = "Due Date is require")
    private Date dueDate;
    @NotBlank(message = "Status is require")
    private String status;
    @NotNull(message = "Status is require")
    @Min(value = 1, message = "amount must greater then 0")
    private Long amount;
    @NotBlank(message = "Status is require")
    private String type;
    @NotNull(message = "Item List is require")
    private String itemList;
}
