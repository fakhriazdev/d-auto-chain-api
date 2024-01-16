package com.danamon.autochain.dto.backoffice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BackOfficeViewResponse <T>{
    private List<BackofficeRolesResponse> roles;
    private T generic;
}

