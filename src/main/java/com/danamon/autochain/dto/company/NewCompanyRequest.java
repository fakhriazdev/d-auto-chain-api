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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewCompanyRequest {
    @NotBlank(message = "company name is required")
    private String companyName;
    @NotBlank(message = "province name is required")
    private String province;
    @NotBlank(message = "city name is required")
    private String city;
    @NotBlank(message = "address name is required")
    private String address;
    @NotBlank(message = "phone_number name is required")
    private String phoneNumber;
    @NotBlank(message = "province name is required")
    private String companyEmail;
    private String accountNumber;
    private Double financingLimit;
    private Double remainingLimit;
    @NotNull(message = "price is required")
    @Min(value = 0, message = "price must be greater than or equal 0")
    private Double price;
//    private MultipartFile image;
}
