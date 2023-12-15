package com.danamon.autochain.service;

import com.danamon.autochain.dto.company.*;
import com.danamon.autochain.dto.partnership.PartnershipResponse;
import com.danamon.autochain.dto.partnership.SearchPartnershipRequest;
import com.danamon.autochain.entity.Company;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;

public interface PartnershipService {
    Page<PartnershipResponse> getAll(SearchPartnershipRequest request);
}
