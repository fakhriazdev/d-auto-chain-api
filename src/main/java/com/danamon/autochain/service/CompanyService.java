package com.danamon.autochain.service;

import com.danamon.autochain.dto.company.NewCompanyRequest;
import com.danamon.autochain.dto.company.NewCompanyResponse;
import com.danamon.autochain.dto.company.SearchCompanyRequest;
import com.danamon.autochain.dto.company.CompanyResponse;
import org.springframework.data.domain.Page;

public interface CompanyService {
    NewCompanyResponse create(NewCompanyRequest request);
    Page<CompanyResponse> getAll(SearchCompanyRequest request);
}
