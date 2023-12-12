package com.danamon.autochain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyResponse {
    private String companyId;
    private String companyName;
    private String province;
    private String city;
    private String address;
    private String phoneNumber;
    private String companyEmail;
    private String accountNumber;
    private Double financingLimit;
    private Double reaminingLimit;
}
