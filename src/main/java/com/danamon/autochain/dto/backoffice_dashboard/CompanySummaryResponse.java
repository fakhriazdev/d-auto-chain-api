package com.danamon.autochain.dto.backoffice_dashboard;

import com.danamon.autochain.dto.company.CompanyResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CompanySummaryResponse {
    private Integer registeredCompany;
    private Integer needAttention;
    private Integer restrictedCompany;
    private List<CompanyResponse> companies;
}
