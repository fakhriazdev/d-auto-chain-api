package com.danamon.autochain.service.impl;

import com.danamon.autochain.constant.ActorType;
import com.danamon.autochain.constant.RoleType;
import com.danamon.autochain.controller.backOffice.BackOfficeUserController;
import com.danamon.autochain.dto.backoffice.BackofficeRolesResponse;
import com.danamon.autochain.dto.auth.BackOfficeRegisterRequest;
import com.danamon.autochain.dto.auth.BackOfficeRegisterResponse;
import com.danamon.autochain.dto.backoffice.BackOfficeUserRequest;
import com.danamon.autochain.dto.backoffice.BackOfficeUserResponse;
import com.danamon.autochain.dto.backoffice.BackOfficeViewResponse;
import com.danamon.autochain.dto.company.CompanyResponse;
import com.danamon.autochain.dto.company.SearchCompanyRequest;
import com.danamon.autochain.entity.*;
import com.danamon.autochain.repository.*;
import com.danamon.autochain.service.BackOfficeUserService;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.util.MailSender;
import com.danamon.autochain.util.RandomPasswordUtil;
import com.danamon.autochain.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackOfficeUserServiceImpl implements BackOfficeUserService {
    private final CredentialRepository credentialRepository;
    private final RolesRepository rolesRepository;
    private final CompanyService companyService;
    private final ValidationUtil validationUtil;
    private final UserRolesRepository userRolesRepository;
    private final BackofficeAccessRepository backofficeAccessRepository;
    private final BackOfficeRepository backOfficeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<BackOfficeUserResponse> getAllBackOfficeUser(BackOfficeUserRequest request) {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Sort.Direction direction = Sort.Direction.fromString(request.getDirection());
        Pageable pageRequest = PageRequest.of(request.getPage() - 1, request.getSize(), direction, "username");

        Page<Credential> credentialByActorAndRoles = null;

        if (request.getRole() == null) {
            credentialByActorAndRoles = credentialRepository.findByActorAndCredentialIdNot(ActorType.BACKOFFICE, principal.getCredentialId(), pageRequest);
        } else {
            credentialByActorAndRoles = credentialRepository.getCredentialByActorAndRoles(principal, request.getRole(), pageRequest);
        }
        return credentialByActorAndRoles.map(this::mapToResponse);

    }

    @Override
    public List<BackofficeRolesResponse> getBackOfficeRoles() {
        List<String> admin = List.of("ADMIN", "RELATIONSHIP_MANAGER", "CREDIT_ANALYST");

        List<Roles> roles = rolesRepository.findAllByRoleNameIn(admin).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Roles Not Found"));
        return roles.stream().map(r -> BackofficeRolesResponse.builder().roleName(r.getRoleName()).id(r.getRoleId()).build()).collect(Collectors.toList());
    }

    @Override
    public BackOfficeViewResponse<?> getAccessibility(SearchCompanyRequest request) {
        List<String> name = List.of(RoleType.ADMIN.name(), RoleType.RELATIONSHIP_MANAGER.name(), RoleType.CREDIT_ANALYST.name());
        List<Roles> roles = rolesRepository.findAllByRoleNameIn(name).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Data Not Found"));
        List<BackofficeRolesResponse> collect = roles.stream().map(r -> BackofficeRolesResponse.builder().roleName(r.getRoleName()).id(r.getRoleId()).build()).toList();

        Page<CompanyResponse> companyResponses = companyService.getAll(request);

        return BackOfficeViewResponse.<Page<CompanyResponse>>builder().roles(collect).generic(companyResponses).build();
    }

    @Override
    public BackOfficeViewResponse<?> getAccessibility(List<RoleType> roleTypes) {
        Map<String, List<String>> accessibility = RoleType.getAccessibility(roleTypes);

        List<String> name = List.of(RoleType.ADMIN.name(), RoleType.RELATIONSHIP_MANAGER.name(), RoleType.CREDIT_ANALYST.name());

        List<Roles> roles = rolesRepository.findAllByRoleNameIn(name).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Data Not Found"));

        List<BackofficeRolesResponse> collect = roles.stream().map(r -> BackofficeRolesResponse.builder().roleName(r.getRoleName()).id(r.getRoleId()).build()).toList();

        return BackOfficeViewResponse.<Map<String, List<String>>>builder()
                .roles(collect)
                .generic(accessibility)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BackOfficeRegisterResponse addBackOfficeUser(BackOfficeRegisterRequest backOfficeUserRequest) {
        try {
            validationUtil.validate(backOfficeUserRequest);
            Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            Roles roles = rolesRepository.findById(backOfficeUserRequest.getRolesList()).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Role ID Not Found"));

            if (RoleType.RELATIONSHIP_MANAGER.getName().equals(roles.getRoleName()) && backOfficeUserRequest.getCompanyRequests().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "You must choose one or more company");
            }

            RandomPasswordUtil passwordUtil = new RandomPasswordUtil();
            HashMap<String, String> mail = new HashMap<>();

            List<UserRole> userRoles = new ArrayList<>();
            String password = passwordUtil.generateRandomPassword(12);

            Credential credential = Credential.builder()
                    .username(backOfficeUserRequest.getUsername())
                    .email(backOfficeUserRequest.getEmail())
                    .actor(ActorType.BACKOFFICE)
                    .roles(userRoles)
                    .password(passwordEncoder.encode(password))
                    .createdBy(principal.getUsername())
                    .createdDate(LocalDateTime.now())
                    .build();

            userRoles.add(UserRole.builder()
                    .role(roles)
                    .credential(credential)
                    .build());
            credentialRepository.saveAndFlush(credential);

            userRolesRepository.saveAllAndFlush(userRoles);

            BackOffice backOffice = BackOffice.builder()
                    .name(backOfficeUserRequest.getName())
                    .credential(credential).build();
            backOfficeRepository.saveAndFlush(backOffice);

            if (roles.getRoleName().equals(RoleType.RELATIONSHIP_MANAGER.getName())) {
                List<Company> companies = companyService.findById(backOfficeUserRequest.getCompanyRequests());

                if (companies.size() != backOfficeUserRequest.getCompanyRequests().size()) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Please Input Valid Company");
                }

                List<BackofficeUserAccess> backofficeUserAccesses = companies.stream().map(access ->
                        BackofficeUserAccess.builder()
                                .backOffice(backOffice)
                                .company(access)
                                .build()
                ).toList();
                backofficeAccessRepository.saveAllAndFlush(backofficeUserAccesses);
            }


            String accountEmail = "<html style='width: 100%;'>" +
                    "<body style='width: 100%'>" +
                    "<div style='width: 100%;'>" +
                    "<header style='color:white; width: 100%; background: #F6833C; padding: 12px 10px; top:0;'>" +
                    "<span><h2 style='text-align: center;'>D-Auto Chain</h2></span>" +
                    "</header>" +
                    "<div style='margin: auto;'>" +
                    "<div><h5><center>Your Account</u></center></h5></div><br>" +
                    "<div><h4><center>Email: " + credential.getEmail() + "</center></h4></div><br>" +
                    "</div>" +
                    "<div><h4><center>Password: " + password + "</center></h4></div><br>" +
                    "</div>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            mail.put("emailBody", accountEmail);

            MailSender.mailer("Register Back Office Account", mail, credential.getEmail());

            return BackOfficeRegisterResponse.builder()
                    .username(credential.getUsername())
                    .email(credential.getEmail())
                    .build();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "user already exist");
        } catch (MessagingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed To Send Email, Please Contact Administrator");
        }
    }

    @Override
    public BackOfficeUserResponse getBackOfficeUserById(String id) {
        Credential credential = credentialRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Data Not Found"));

        return mapToResponse(credential);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBackofficeUser(BackOfficeUserController.EditBackOfficeUser request) {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //set up for user list
        List<UserRole> userRoles = new ArrayList<>();

        //get role
        Roles roles = rolesRepository.findById(request.roles().stream().findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT))).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT));

        //get BackOffice Data
        BackOffice backOffice = backOfficeRepository.findById(request.id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT));

        backOffice.setName(request.name());

        // get Credential Data
        Credential credential = backOffice.getCredential();
        userRolesRepository.deleteAll(credential.getRoles());

        credential.setUsername(request.username());
        credential.setEmail(request.email());
        credential.setModifiedBy(principal.getUsername2());
        credential.setModifiedDate(LocalDateTime.now());

        userRoles.add(
                UserRole.builder()
                        .role(roles)
                        .credential(credential)
                        .build()
        );

        credential.setRoles(userRoles);

        if (RoleType.RELATIONSHIP_MANAGER.getName().equals(roles.getRoleName())) {
            // checking company
            List<Company> companies = companyService.findById(request.companies());

            if (companies.isEmpty() && request.companies().size() != companies.size()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Please Input Valid Company ID");
            }

            backofficeAccessRepository.deleteAll(backOffice.getBackofficeUserAccesses());

            List<BackofficeUserAccess> userAccesses = companies.stream().map(c ->
                        BackofficeUserAccess.builder()
                                .company(c)
                                .backOffice(backOffice)
                                .build()
            ).collect(Collectors.toList());

            backOffice.setBackofficeUserAccesses(userAccesses);
        }

        backOfficeRepository.saveAndFlush(backOffice);
        credentialRepository.saveAndFlush(credential);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(String id) {
        BackOffice backOffice = backOfficeRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Data Not Found"));

        backofficeAccessRepository.customDelete(backOffice.getBackoffice_id());

        backOfficeRepository.delete(backOffice);
    }

    private BackOfficeUserResponse mapToResponse(Credential data) {
        List<String> collect = data.getRoles().stream().map(r -> RoleType.valueOf(r.getRole().getRoleName()).getName()).toList();
        return BackOfficeUserResponse.builder()
                .id(data.getBackOffice().getBackoffice_id())
                .username(data.getUsername())
                .email(data.getEmail())
                .roles(collect.stream().findFirst().orElse(null))
                .build();
    }

}
