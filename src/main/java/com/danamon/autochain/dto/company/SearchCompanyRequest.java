package com.danamon.autochain.dto.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchCompanyRequest {
    private Integer page;
    private Integer size;
    private String direction;
    private String status;
    private String name;
    private String sortBy;
}
