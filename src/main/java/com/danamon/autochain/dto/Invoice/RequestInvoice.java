package com.danamon.autochain.dto.Invoice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestInvoice {
    @NotBlank(message = "Recipient is require")
    private String recipientId;
    @NotBlank(message = "Due Date is require")
    private Date dueDate;
    @NotBlank(message = "Status is require")
    private String status;
    @NotNull(message = "Status is require")
    private Integer amount;
    @NotBlank(message = "Status is require")
    private String type;
}
