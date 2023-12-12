package com.danamon.autochain.service;

import com.danamon.autochain.dto.company.NewCompanyRequest;
import com.danamon.autochain.dto.company.SearchCompanyRequest;
import com.danamon.autochain.dto.company.CompanyResponse;
import org.springframework.data.domain.Page;

public interface CompanyService {
    CompanyResponse create(NewCompanyRequest request);
    Page<CompanyResponse> getAll(SearchCompanyRequest request);
}
