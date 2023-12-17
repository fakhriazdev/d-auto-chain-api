package com.danamon.autochain.dto.partnership;

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
public class NewPartnershipRequest {
    @NotBlank(message = "company id is required")
    private String companyId;
    @NotBlank(message = "partnership id is required")
    private String partnershipId;
}
