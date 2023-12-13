package com.danamon.autochain.service.impl;

import com.danamon.autochain.constant.UserRoleType;
import com.danamon.autochain.dto.company.NewCompanyRequest;
import com.danamon.autochain.dto.company.NewCompanyResponse;
import com.danamon.autochain.dto.company.SearchCompanyRequest;
import com.danamon.autochain.dto.company.CompanyResponse;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.CompanyFile;
import com.danamon.autochain.entity.User;
import com.danamon.autochain.repository.CompanyRepository;
import com.danamon.autochain.service.CompanyFileService;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.service.UserService;
import com.danamon.autochain.util.RandomPasswordUtil;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;
    private final ValidationUtil validationUtil;
    private final CompanyFileService companyFileService;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final RandomPasswordUtil randomPasswordUtil;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public NewCompanyResponse create(NewCompanyRequest request) {
        validationUtil.validate(request);

        List<CompanyFile> companyFiles = request.getMultipartFiles().stream().map(multipartFile ->
                companyFileService.createFile(multipartFile)
        ).collect(Collectors.toList());

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
                .companyFiles(companyFiles)
                .build();
        Company companySaved = companyRepository.saveAndFlush(company);

        String password = randomPasswordUtil.generateRandomPassword(12);

        User user = User.builder()
                .company_id(company)
                .email(request.getCompanyEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(password))
                .user_type(UserRoleType.SUPER_ADMIN)
                .build();
        userService.create(user);

        companySaved.setUser(user);

        return newMapToResponse(companySaved, password);
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
                .username(company.getUser().getUsername())
                .build();
    }

    private NewCompanyResponse newMapToResponse(Company company, String password) {
        return NewCompanyResponse.builder()
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
                .username(company.getUser().getUsername())
                .password(password)
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
