package com.danamon.autochain.dto.partnership;

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
public class PartnershipResponse {
    private String partnershipId;
    private String companyId;
    private String partnerId;
    private String partnerStatus;
    private String partnerRequestedDate;
    private String partnerConfirmationDate;
    private String requestedBy;
    private String confirmedBy;
}
