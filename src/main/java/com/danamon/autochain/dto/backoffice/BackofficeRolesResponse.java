package com.danamon.autochain.dto.backoffice;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BackofficeRolesResponse {
    private String roleName;
    private String id;
}
