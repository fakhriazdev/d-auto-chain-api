package com.danamon.autochain.dto.Invoice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemList {
    private String itemsName;
    private Integer itemsQuantity;
    private Long unitPrice;
}
