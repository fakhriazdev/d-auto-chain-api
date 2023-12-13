package com.danamon.autochain.dto.company;

import com.danamon.autochain.dto.FileResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewCompanyResponse {
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
    private List<FileResponse> files;
    private String username;
    private String password;
}
