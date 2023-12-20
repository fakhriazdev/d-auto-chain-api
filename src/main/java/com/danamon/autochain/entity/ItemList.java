package com.danamon.autochain.entity;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemList {
    private String itemsName;
    private Integer itemsQuantity;
    private Long unitPrice;
}
