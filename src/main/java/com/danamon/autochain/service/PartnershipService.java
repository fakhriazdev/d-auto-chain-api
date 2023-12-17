package com.danamon.autochain.service;

import com.danamon.autochain.dto.company.CompanyResponse;
import com.danamon.autochain.dto.company.NewCompanyRequest;
import com.danamon.autochain.dto.company.NewCompanyResponse;
import com.danamon.autochain.dto.partnership.NewPartnershipRequest;
import com.danamon.autochain.dto.partnership.PartnershipResponse;
import com.danamon.autochain.dto.partnership.SearchPartnershipRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PartnershipService {
    Page<PartnershipResponse> getAll(SearchPartnershipRequest request);
    PartnershipResponse addPartnership(NewPartnershipRequest request);
}
