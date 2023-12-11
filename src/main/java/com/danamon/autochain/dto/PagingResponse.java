package com.danamon.autochain.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PagingResponse {
    private Integer totalPages;
    private Long count; // jumlah data yg ada di database
    private Integer page;
    private Integer size;
}
