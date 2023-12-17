package com.danamon.autochain.service.impl;

import com.danamon.autochain.constant.UserRoleType;
import com.danamon.autochain.dto.FileResponse;
import com.danamon.autochain.dto.auth.OtpResponse;
import com.danamon.autochain.dto.company.*;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.CompanyFile;
import com.danamon.autochain.entity.User;
import com.danamon.autochain.repository.CompanyRepository;
import com.danamon.autochain.service.CompanyFileService;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.service.UserService;
import com.danamon.autochain.util.MailSender;
import com.danamon.autochain.util.OTPGenerator;
import com.danamon.autochain.util.RandomPasswordUtil;
import com.danamon.autochain.util.ValidationUtil;
import jakarta.persistence.Column;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.mapping.Any;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
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

    @Override
    public List<CompanyResponse> getNonPartnership(String companyId) {
        Company company = findByIdOrThrowNotFound(companyId);
        List<Company> companies = companyRepository.findAll();

        return companies.stream()
                .filter(c -> c.getCompany_id() != company.getCompany_id())
                .filter(c -> company.getPartnerships().stream()
                .noneMatch(c2 -> c2.getPartner().getCompany_id().equals(c.getCompany_id())))
                .map(this::mapToResponse).collect(Collectors.toList());
    }
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
                .email(request.getEmailUser())
                .username(request.getUsername())
                .password(passwordEncoder.encode(password))
                .user_type(UserRoleType.SUPER_ADMIN)
                .build();
        userService.create(user);

        companySaved.setUser(user);

        HashMap<String, String> info = new HashMap<>();

        try {
            info.put("Email: ",request.getCompanyEmail() +"<br>");
            info.put("Password: ", password +"<br>");

            MailSender.mailer("Your Company Account", info, request.getEmailUser());
        }  catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

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

    @Transactional(readOnly = true)
    @Override
    public Company getById(String id) {
        return findByIdOrThrowNotFound(id);
    }

    @Transactional(readOnly = true)
    @Override
    public CompanyResponse findById(String id) {
        Company company = findByIdOrThrowNotFound(id);
        return mapToResponse(company);
    }

    private Company findByIdOrThrowNotFound(String id) {
        return companyRepository.findById(id).orElseThrow(() -> new RuntimeException("company not found"));
    }

    @Transactional(readOnly = true)
    @Override
    public Resource getCompanyFilesByIdFile(String idFile) {
        CompanyFile companyFile = companyFileService.findById(idFile);
        return companyFileService.findByPath(companyFile.getPath());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CompanyResponse update(UpdateCompanyRequest request) {
        validationUtil.validate(request);

        Company companyOld = findByIdOrThrowNotFound(request.getId());
        companyOld.setCompanyName(request.getCompanyName());
        companyOld.setProvince(request.getProvince());
        companyOld.setCity(request.getCity());
        companyOld.setAddress(request.getAddress());
        companyOld.setPhoneNumber(request.getPhoneNumber());
        companyOld.setCompanyEmail(request.getCompanyEmail());
        companyOld.setAccountNumber(request.getAccountNumber());
        companyOld.setFinancingLimit(request.getFinancingLimit());
        companyOld.setRemainingLimit(request.getReaminingLimit());
        Company company = companyRepository.saveAndFlush(companyOld);

        return mapToResponse(company);
    }

    private CompanyResponse mapToResponse(Company company) {
        List<FileResponse> fileResponses = company.getCompanyFiles().stream().map(
                companyFiles -> FileResponse.builder()
                        .filename(companyFiles.getName())
                        .url("/api/companies/" + companyFiles.getId() + "/file")
                        .build()
        ).collect(Collectors.toList());
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
                .userId(company.getUser().getUser_id())
                .username(company.getUser().getUsername())
                .emailUser(company.getUser().getEmail())
                .files(fileResponses)
                .build();
    }

    private NewCompanyResponse newMapToResponse(Company company, String password) {
        List<FileResponse> fileResponses = company.getCompanyFiles().stream().map(
                companyFiles -> FileResponse.builder()
                        .filename(companyFiles.getName())
                        .url("/api/companies/" + companyFiles.getId() + "/file")
                        .build()
        ).collect(Collectors.toList());
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
                .userId(company.getUser().getUser_id())
                .username(company.getUser().getUsername())
                .emailUser(company.getUser().getEmail())
                .password(password)
                .files(fileResponses)
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
