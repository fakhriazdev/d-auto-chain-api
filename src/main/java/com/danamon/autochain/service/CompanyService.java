package com.danamon.autochain.service;

import com.danamon.autochain.dto.company.NewCompanyRequest;
import com.danamon.autochain.dto.company.NewCompanyResponse;
import com.danamon.autochain.dto.company.SearchCompanyRequest;
import com.danamon.autochain.dto.company.CompanyResponse;
import com.danamon.autochain.entity.Company;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;

public interface CompanyService {
    NewCompanyResponse create(NewCompanyRequest request);
    Page<CompanyResponse> getAll(SearchCompanyRequest request);
    Company getById(String id);
    CompanyResponse findById(String id);
    Resource getCompanyFilesByIdFile(String idFile);
}
