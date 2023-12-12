package com.danamon.autochain.service.impl;

import com.danamon.autochain.dto.company.NewCompanyRequest;
import com.danamon.autochain.dto.company.SearchCompanyRequest;
import com.danamon.autochain.dto.company.CompanyResponse;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.repository.CompanyRepository;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.util.ValidationUtil;
import jakarta.persistence.Column;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;
    private final ValidationUtil validationUtil;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CompanyResponse create(NewCompanyRequest request) {
        validationUtil.validate(request);
//        FileRequest fileRequest = FileRequest.builder()
//                .multipartFile(request.getImage())
//                .type(FileType.MENU_IMAGE)
//                .build();
//        File menuImage = fileService.createFile(fileRequest);
        Company company = Company.builder()
                .companyName(request.getCompanyName())
                .province(request.getProvince())
                .city(request.getCity())
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .companyEmail(request.getCompanyEmail())
                .accountNumber(request.getAccountNumber())
                .financingLimit(request.getFinancingLimit())
                .remainingLimit(request.getRemainingLimit())
                .build();
        companyRepository.saveAndFlush(company);
        return mapToResponse(company);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<CompanyResponse> getAll(SearchCompanyRequest request) {
        Specification<Company> specification = getCompanySpecification(request);
        Sort.Direction direction = Sort.Direction.fromString(request.getDirection());
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), direction, request.getSortBy());
        Page<Company> companies = companyRepository.findAll(specification, pageable);
        return companies.map(this::mapToResponse);
    }

    private CompanyResponse mapToResponse(Company company) {
        return CompanyResponse.builder()
                .companyId(company.getCompany_id())
                .companyName(company.getCompanyName())
                .province(company.getProvince())
                .city(company.getCity())
                .address(company.getAddress())
                .phoneNumber(company.getPhoneNumber())
                .companyEmail(company.getCompanyEmail())
                .accountNumber(company.getAccountNumber())
                .financingLimit(company.getFinancingLimit())
                .reaminingLimit(company.getRemainingLimit())
                .build();
    }

    private Specification<Company> getCompanySpecification(SearchCompanyRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getName() != null) {
                Predicate name = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("companyName")),
                        "%" + request.getName().toLowerCase() + "%"
                );
                predicates.add(name);
            }

            return query
                    .where(predicates.toArray(new Predicate[]{}))
                    .getRestriction();
        };
    }
}
