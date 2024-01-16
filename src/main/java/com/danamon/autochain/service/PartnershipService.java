package com.danamon.autochain.service;

import com.danamon.autochain.dto.partnership.NewPartnershipRequest;
import com.danamon.autochain.dto.partnership.PartnershipResponse;
import com.danamon.autochain.dto.partnership.SearchPartnershipRequest;
import org.springframework.data.domain.Page;

public interface PartnershipService {
    Page<PartnershipResponse> getAll(String id, SearchPartnershipRequest request);
    PartnershipResponse addPartnership(NewPartnershipRequest request);
    PartnershipResponse acceptPartnership(String partnershipId);
    String rejectPartnership(String partnershipId);
}
