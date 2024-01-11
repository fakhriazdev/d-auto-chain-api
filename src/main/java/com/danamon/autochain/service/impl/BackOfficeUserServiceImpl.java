package com.danamon.autochain.service.impl;

import com.danamon.autochain.constant.ActorType;
import com.danamon.autochain.constant.RoleType;
import com.danamon.autochain.controller.backOffice.BackOfficeUserController;
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
import com.danamon.autochain.service.CredentialService;
import com.danamon.autochain.util.MailSender;
import com.danamon.autochain.util.RandomPasswordUtil;
import com.danamon.autochain.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BackOfficeUserServiceImpl implements BackOfficeUserService {
    private final CredentialRepository credentialRepository;
    private final RolesRepository rolesRepository;
    private final CompanyService companyService;
    private final ValidationUtil validationUtil;
    private final UserRolesRepository userRolesRepository;
    private final UserAccsessRepository userAccessRepository;
    private final BackOfficeRepository backOfficeRepository;
    private final CredentialService credentialService;

    @Override
    public Page<BackOfficeUserResponse> getAllBackOfficeUser(BackOfficeUserRequest request) {
        Sort.Direction direction = Sort.Direction.fromString(request.getDirection());
        Pageable pageRequest = PageRequest.of(request.getPage() - 1, request.getSize(), direction, "username");

        Page<Credential> credentialByActorAndRoles = credentialRepository.getCredentialByActorAndRoles(request.getRole(), pageRequest);
        return credentialByActorAndRoles.map(this::mapToResponse);

    }

    @Override
    public BackOfficeViewResponse<?> getAccessibility(SearchCompanyRequest request) {
        List<String> name = List.of(RoleType.ADMIN.getName(), RoleType.RELATIONSHIP_MANAGER.getName(), RoleType.CREDIT_ANALYST.getName());
        List<Roles> dataNotFound = rolesRepository.findByRoleNameIn(name).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Data Not Found"));

        Page<CompanyResponse> companyResponses = companyService.getAll(request);

        return BackOfficeViewResponse.<Page<CompanyResponse>>builder().roles(dataNotFound).generic(companyResponses).build();
    }

    @Override
    public BackOfficeViewResponse<?> getAccessibility(List<RoleType> roleTypes) {
        Map<String, List<String>> accessibility = RoleType.getAccessibility(roleTypes);
        List<String> name = List.of(RoleType.ADMIN.getName(), RoleType.RELATIONSHIP_MANAGER.getName(), RoleType.CREDIT_ANALYST.getName());
        List<Roles> roles = rolesRepository.findByRoleNameIn(name).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Data Not Found"));
        return BackOfficeViewResponse.<Map<String, List<String>>>builder()
                .roles(roles)
                .generic(accessibility)
                .build();

    }

    @Override
    public BackOfficeRegisterResponse addBackOfficeUser(BackOfficeRegisterRequest backOfficeUserRequest) {
        try {
            Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            validationUtil.validate(backOfficeUserRequest);
            if (backOfficeUserRequest.getRolesList().equals(RoleType.RELATIONSHIP_MANAGER.getName()) && backOfficeUserRequest.getCompanyRequests().size() <= 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "You must choose one or more company");
            }
            Roles roles = rolesRepository.findById(backOfficeUserRequest.getRolesList()).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Role ID Not Found"));

            RandomPasswordUtil passwordUtil = new RandomPasswordUtil();
            HashMap<String, String> mail = new HashMap<>();

            List<Company> companies = companyService.findById(backOfficeUserRequest.getCompanyRequests());
            List<UserRole> userRoles = new ArrayList<>();

            Credential credential = Credential.builder()
                    .username(backOfficeUserRequest.getUsername())
                    .email(backOfficeUserRequest.getEmail())
                    .actor(ActorType.BACKOFFICE)
                    .roles(userRoles)
                    .password(passwordUtil.generateRandomPassword(12))
                    .createdBy(principal.getUsername())
                    .createdDate(LocalDateTime.now())
                    .build();

            credentialRepository.saveAndFlush(credential);

            userRoles.add(UserRole.builder()
                    .role(roles)
                    .credential(credential)
                    .build());

            userRolesRepository.saveAllAndFlush(userRoles);

            BackOffice backOffice = BackOffice.builder()
                    .credential(credential).build();

            List<UserAccsess> userAccsessList = companies.stream().map(c -> UserAccsess.builder()
                    .company(c)
                    .build()
            ).collect(Collectors.toList());

            userAccessRepository.saveAllAndFlush(userAccsessList);
            backOfficeRepository.saveAndFlush(backOffice);

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
                    "<div><h4><center>Password: " + credential.getPassword() + "</center></h4></div><br>" +
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
    public void updateBackofficeUser(BackOfficeUserController.EditBackOfficeUser request) {

    }
    private BackOfficeUserResponse mapToResponse(Credential data){
        List<String> collect = data.getRoles().stream().map(r -> RoleType.valueOf(r.getRole().getRoleName()).getName()).toList();
        return BackOfficeUserResponse.builder()
                .userId(data.getCredentialId())
                .username(data.getUsername())
                .email(data.getEmail())
                .roles(collect.stream().findFirst().orElse(null))
                .build();
    }

}
