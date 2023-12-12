package com.danamon.autochain.service;

import com.danamon.autochain.dto.request.SearchCompanyRequest;
import com.danamon.autochain.dto.response.CompanyResponse;
import org.springframework.data.domain.Page;

public interface CompanyService {
    Page<CompanyResponse> getAll(SearchCompanyRequest request);
}
