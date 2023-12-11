package com.danamon.autochain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataResponse<T>{
    private String message;
    private Integer statusCode;
    private T data;
    private PagingResponse paging;
}
