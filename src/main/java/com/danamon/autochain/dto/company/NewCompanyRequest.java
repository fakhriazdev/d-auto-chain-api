package com.danamon.autochain.dto.company;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewCompanyRequest {
    @NotBlank(message = "company name is required")
    private String companyName;
    @NotBlank(message = "province is required")
    private String province;
    @NotBlank(message = "city is required")
    private String city;
    @NotBlank(message = "address is required")
    private String address;
    @NotBlank(message = "phone_number is required")
    private String phoneNumber;
    @NotBlank(message = "province is required")
    private String companyEmail;
    @NotBlank(message = "account number is required")
    private String accountNumber;
    @NotNull(message = "financing limit is required")
    @Min(value = 0, message = "financing limit must be greater than or equal 0")
    private Double financingLimit;
    @NotNull(message = "reamining limit is required")
    @Min(value = 0, message = "reamining limit must be greater than or equal 0")
    private Double remainingLimit;
    private List<MultipartFile> multipartFiles;
    @NotBlank(message = "username is required")
    private String username;
}
