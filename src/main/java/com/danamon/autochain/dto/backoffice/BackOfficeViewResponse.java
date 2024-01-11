package com.danamon.autochain.dto.backoffice;

import com.danamon.autochain.entity.Roles;
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
    private List<Roles> roles;
    private T generic;
}

