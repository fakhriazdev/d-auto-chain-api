package com.danamon.autochain.service;

import com.danamon.autochain.dto.backoffice_dashboard.CompanySummaryResponse;
import com.danamon.autochain.dto.company.*;
import com.danamon.autochain.entity.Company;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface CompanyService {
    CompanyResponse create(NewCompanyRequest request);
    Page<CompanyResponse> getAll(SearchCompanyRequest request);
    Company getById(String id);
    CompanyResponse findById(String id);
    Resource getCompanyFilesByIdFile(String idFile);
    CompanyResponse update(UpdateCompanyRequest request);
    List<CompanyResponse> getNonPartnership(String id);
    List<Company> getCompaniesNameLike(String companyName);
    List<Company> findById(List<String> id);
    CompanySummaryResponse getCompanySummary();
}
