package com.danamon.autochain.service.impl;

import com.danamon.autochain.constant.ActorType;
import com.danamon.autochain.constant.RoleType;
import com.danamon.autochain.dto.FileResponse;
import com.danamon.autochain.dto.auth.UserRegisterResponse;
import com.danamon.autochain.dto.company.CompanyResponse;
import com.danamon.autochain.dto.company.SearchCompanyRequest;
import com.danamon.autochain.dto.manage_user.ManageUserResponse;
import com.danamon.autochain.dto.manage_user.NewUserRequest;
import com.danamon.autochain.dto.manage_user.SearchManageUserRequest;
import com.danamon.autochain.dto.manage_user.UpdateUserRequest;
import com.danamon.autochain.entity.*;
import com.danamon.autochain.repository.*;
import com.danamon.autochain.security.BCryptUtil;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.service.CredentialService;
import com.danamon.autochain.service.ManageUserService;
import com.danamon.autochain.service.UserService;
import com.danamon.autochain.util.MailSender;
import com.danamon.autochain.util.RandomPasswordUtil;
import com.danamon.autochain.util.ValidationUtil;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.management.relation.Role;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManageUserServiceImpl implements ManageUserService {
    private final UserRepository userRepository;
    private final RolesRepository rolesRepository;
    private final UserRolesRepository userRolesRepository;
    private final CredentialRepository credentialRepository;
    private final ValidationUtil validationUtil;
    private final RandomPasswordUtil randomPasswordUtil;
    private final BCryptUtil bCryptUtil;
    private final CompanyService companyService;
    private final UserAccsessRepository userAccsessRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ManageUserResponse> getAllUser(SearchManageUserRequest request) {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findUserByCredential(principal).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credential invalid"));

        List<Credential> credentials = new ArrayList<>();

        if (request.getAccess() != null) {
            try {
                Optional<Roles> access = rolesRepository.findByRoleName(request.getAccess());

                if (access.isPresent()) {
                    List<UserRole> userRoles = userRolesRepository.findAllByRole(access.get());

                    credentials.addAll(userRoles.stream().map(userRole -> credentialRepository.findById(userRole.getCredential().getCredentialId()).get()).collect(Collectors.toList()));
                }
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found");
            }
        } else {
            Optional<Roles> access = rolesRepository.findByRoleName("SUPER_USER");

            if (access.isPresent()) {
                List<UserRole> userRoles = userRolesRepository.findAllByRoleIsNot(access.get());

                credentials.addAll(userRoles.stream().map(userRole -> credentialRepository.findById(userRole.getCredential().getCredentialId()).get()).collect(Collectors.toList()));
            }
        }

        Specification<User> specification = getManageUserSpesification(request, user, credentials);
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize());
        Page<User> users = userRepository.findAll(specification, pageable);

        return users.map(u -> mapToResponse(u));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ManageUserResponse createUser(NewUserRequest request) {
        try {
            Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User userLogin = userRepository.findUserByCredential(principal).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credential invalid"));

            validationUtil.validate(request);

            String password = randomPasswordUtil.generateRandomPassword(12);

            List<UserRole> userRoles = new ArrayList<>();

            Credential credential = Credential.builder()
                    .email(request.getEmail())
                    .username(request.getUsername())
                    .password(bCryptUtil.hashPassword(password))
                    .actor(ActorType.USER)
                    .roles(userRoles)
                    .createdDate(LocalDateTime.now())
                    .createdBy(principal.getUsername())
                    .build();

            request.getAccess().forEach(roleName -> {
                Roles role = rolesRepository.findByRoleName(roleName).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "roles not exist"));
                        userRoles.add(
                                UserRole.builder()
                                        .role(role)
                                        .credential(credential)
                                        .build()
                        );
                    }
            );

            credentialRepository.saveAndFlush(credential);

            List<UserAccsess> userAccsesses = new ArrayList<>();

            User user = User.builder()
                    .name(request.getName())
                    .company(userLogin.getCompany())
                    .credential(credential)
                    .userAccsess(userAccsesses)
                    .build();

            request.getCompanyIds().forEach(companyId -> {
                Company company = companyService.getById(companyId);
                    userAccsesses.add(
                            UserAccsess.builder()
                                        .company(company)
                                        .user(user)
                                        .build()
                        );
                    }
            );

            userRepository.saveAndFlush(user);

            HashMap<String, String> info = new HashMap<>();

            try {
                String accountEmail = "<html style='width: 100%;'>" +
                        "<body style='width: 100%'>" +
                        "<div style='width: 100%;'>" +
                        "<header style='color:white; width: 100%; background: #F6833C; padding: 12px 10px; top:0;'>" +
                        "<span><h2 style='text-align: center;'>D-Auto Chain</h2></span>" +
                        "</header>" +
                        "<div style='margin: auto;'>" +
                        "<div><h5><center>Your Account</u></center></h5></div><br>" +
                        "<div><h4><center>Email: "+request.getEmail()+"</center></h4></div><br>" +
                        "</div>" +
                        "<div><h4><center>Password: "+password+"</center></h4></div><br>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>";

                info.put("emailBody", accountEmail);

                MailSender.mailer("Here Your Company Account", info, request.getEmail());
            }  catch (Exception e){
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return mapToResponse(user);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ManageUserResponse updateUser(UpdateUserRequest request) {
        try {
            Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User userLogin = userRepository.findUserByCredential(principal).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credential invalid"));

            validationUtil.validate(request);

            Optional<Credential> credential = credentialRepository.findByEmail(request.getEmail());

            if (credential.isPresent()) {
                credential.get().setUsername(request.getUsername());
                credential.get().setEmail(request.getEmail());
                credential.get().setModifiedBy(userLogin.getCredential().getUsername());
                credential.get().setModifiedDate(LocalDateTime.now());

                // check if exist in user role
                request.getAccess().forEach(roleName -> {
                            Roles role = rolesRepository.findByRoleName(roleName).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "roles not exist"));

                            Optional<UserRole> userRole = userRolesRepository.findByCredentialAndRole(credential.get(), role);

                            if (userRole.isEmpty()) {
                                UserRole newRole = UserRole.builder()
                                        .role(role)
                                        .credential(credential.get())
                                        .build();

                                credential.get().getRoles().add(newRole);

                                userRolesRepository.save(newRole);
                            }
                        }
                );

                // check if doesnt exist in request
                credential.get().getRoles().removeIf(userRole -> {
                    boolean found = request.getAccess().stream()
                            .anyMatch(s -> userRole.getRole().getRoleName().equals(s));

                    if (!found) {
                        userRolesRepository.delete(userRole);
                        return true;
                    }

                    return false;
                });

                credentialRepository.saveAndFlush(credential.get());
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Credential not found");
            }

            User user = getById(request.getUserId());
            user.setName(request.getName());
            user.setCredential(credential.get());

            // check if exist in user access
            request.getCompanyIds().forEach(companyId -> {
                Company company = companyService.getById(companyId);

                Optional<UserAccsess> userAccess = userAccsessRepository.findByUserAndCompany(user, company);

                if (userAccess.isEmpty()) {
                    UserAccsess newUserAccess = UserAccsess.builder()
                            .company(company)
                            .user(user)
                            .build();

                    user.getUserAccsess().add(newUserAccess);

                    userAccsessRepository.save(newUserAccess);
                }
            }
            );

            // check if doesnt exist in request
            user.getUserAccsess().removeIf(userAccsess -> {
                boolean found = request.getCompanyIds().stream()
                        .anyMatch(s -> userAccsess.getCompany().getCompany_id().equals(s));

                if (!found) {
                    userAccsessRepository.delete(userAccsess);
                    return true;
                }

                return false;
            });

            userRepository.saveAndFlush(user);

            return mapToResponse(user);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public User getById(String id) {
        return findByIdOrThrowNotFound(id);
    }

    private User findByIdOrThrowNotFound(String id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("user not found"));
    }

    private ManageUserResponse mapToResponse(User user) {
        List<String> roles = user.getCredential().getRoles().stream().map(userRole -> userRole.getRole().getRoleName()).collect(Collectors.toList());
        List<String> companyIds = user.getUserAccsess().stream().map(userAccsess -> userAccsess.getCompany().getCompany_id()).collect(Collectors.toList());

        return ManageUserResponse.builder()
                .userId(user.getUser_id())
                .username(user.getCredential().getUsername())
                .name(user.getName())
                .email(user.getCredential().getEmail())
                .access(roles)
                .companyIds(companyIds)
                .build();
    }

    private Specification<User> getManageUserSpesification(SearchManageUserRequest request, User user, List<Credential> credentials) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(root.get("credential").in(credentials));

            Predicate company = criteriaBuilder.equal(
                    root.get("company"),
                    user.getCompany()
            );

            predicates.add(company);

            if (request.getName() != null) {
                Predicate name = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
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

