package com.danamon.autochain.service.impl;

import com.danamon.autochain.constant.ActorType;
import com.danamon.autochain.constant.RoleType;
import com.danamon.autochain.dto.FileResponse;
import com.danamon.autochain.dto.company.*;
import com.danamon.autochain.entity.*;
import com.danamon.autochain.repository.*;
import com.danamon.autochain.security.BCryptUtil;
import com.danamon.autochain.service.CompanyFileService;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.util.MailSender;
import com.danamon.autochain.util.RandomPasswordUtil;
import com.danamon.autochain.util.ValidationUtil;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;
    private final ValidationUtil validationUtil;
    private final CompanyFileService companyFileService;
    private final UserRepository userRepository;
    private final RolesRepository rolesRepository;
    private final RandomPasswordUtil randomPasswordUtil;
    private final CredentialRepository credentialRepository;
    private final UserRolesRepository userRolesRepository;
    private final BCryptUtil bCryptUtil;

    @Override
    public List<CompanyResponse> getNonPartnership(String companyId) {
        Company company = findByIdOrThrowNotFound(companyId);
        List<Company> companies = companyRepository.findAll();

        List<String> existingPartnershipCompanies = company.getPartnerships().stream()
                .map(partnership -> partnership.getPartner().getCompany_id())
                .collect(Collectors.toList());

        List<Company> filteredCompanies = companies.stream()
                .filter(c -> !c.getCompany_id().equals(company.getCompany_id()))
                .filter(c -> !existingPartnershipCompanies.contains(c.getCompany_id()))
                .collect(Collectors.toList());

        return filteredCompanies.stream().map(c -> mapToResponse(c)).collect(Collectors.toList());
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

        Credential credential = Credential.builder()
                .email(request.getEmailUser())
                .username(request.getUsername())
                .password(bCryptUtil.hashPassword(password))
                .actor(ActorType.USER)
                .build();

        credentialRepository.saveAndFlush(credential);

        User user = User.builder()
                .company(companySaved)
                .credential(credential)
                .build();

        userRepository.saveAndFlush(user);
        companySaved.setUser(user);

        Roles role = rolesRepository.findByRoleName(RoleType.SUPER_USER.toString()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ROLE not found"));
        UserRole userRole = UserRole.builder()
                .role(role)
                .credential(credential)
                .build();

        credential.setRoles(List.of(userRole));

        userRolesRepository.saveAndFlush(userRole);

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
        Company company = companyRepository.saveAndFlush(companyOld);

        if (request.getIsGeneratePassword()) {
            Credential credential = credentialRepository.findByEmail(request.getEmailUser()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

            String newPassword = randomPasswordUtil.generateRandomPassword(12);

            credential.setPassword(bCryptUtil.hashPassword(newPassword));
            credential.setModifiedDate(LocalDateTime.now());
            credentialRepository.saveAndFlush(credential);

            HashMap<String, String> info = new HashMap<>();

            try {
                info.put("Email: ",request.getCompanyEmail() +"<br>");
                info.put("Password: ", newPassword +"<br>");

                MailSender.mailer("Your Company Account", info, request.getEmailUser());
            }  catch (Exception e){
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        if (request.getMultipartFiles() != null) {
            List<CompanyFile> companyFiles = request.getMultipartFiles().stream().map(multipartFile ->
                    companyFileService.createFile(multipartFile)
            ).collect(Collectors.toList());

            if(companyOld.getCompanyFiles().size() != 0) {
                List<CompanyFile> oldFiles = new ArrayList<>(companyOld.getCompanyFiles());
                companyFiles.addAll(oldFiles);
            }

            company.setCompanyFiles(companyFiles);
        }

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
                .username(company.getUser().getCredential().getUsername())
                .emailUser(company.getUser().getCredential().getEmail())
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
                .username(company.getUser().getCredential().getUsername())
                .emailUser(company.getUser().getCredential().getEmail())
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
