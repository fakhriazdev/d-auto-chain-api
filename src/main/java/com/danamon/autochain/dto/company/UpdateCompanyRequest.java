package com.danamon.autochain.dto.company;

import com.danamon.autochain.dto.FileResponse;
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
public class UpdateCompanyRequest {
    private String id;
    private String companyName;
    private String province;
    private String city;
    private String address;
    private String phoneNumber;
    private String companyEmail;
    private String accountNumber;
    private List<MultipartFile> multipartFiles;
    private String emailUser;
    private Boolean isGeneratePassword;
}
