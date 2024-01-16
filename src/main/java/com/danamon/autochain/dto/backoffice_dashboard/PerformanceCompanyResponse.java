package com.danamon.autochain.dto.backoffice_dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PerformanceCompanyResponse {
    private String company_name;
    private Double value;
}
