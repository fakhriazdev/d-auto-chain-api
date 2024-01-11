package com.danamon.autochain.dto.backoffice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BackOfficeUserRequest {
    private Integer page;
    private Integer size;
    private String direction;
    private String role;
}
