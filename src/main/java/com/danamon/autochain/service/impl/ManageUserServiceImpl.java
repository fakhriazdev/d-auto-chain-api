package com.danamon.autochain.service.impl;

import com.danamon.autochain.constant.RoleType;
import com.danamon.autochain.dto.FileResponse;
import com.danamon.autochain.dto.auth.UserRegisterResponse;
import com.danamon.autochain.dto.company.CompanyResponse;
import com.danamon.autochain.dto.company.SearchCompanyRequest;
import com.danamon.autochain.dto.manage_user.ManageUserResponse;
import com.danamon.autochain.dto.manage_user.SearchManageUserRequest;
import com.danamon.autochain.entity.*;
import com.danamon.autochain.repository.CredentialRepository;
import com.danamon.autochain.repository.RolesRepository;
import com.danamon.autochain.repository.UserRepository;
import com.danamon.autochain.repository.UserRolesRepository;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.service.CredentialService;
import com.danamon.autochain.service.ManageUserService;
import com.danamon.autochain.service.UserService;
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
import java.util.ArrayList;
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

    private ManageUserResponse mapToResponse(User user) {
        List<String> roles = user.getCredential().getRoles().stream().map(userRole -> userRole.getRole().getRoleName()).collect(Collectors.toList());

        return ManageUserResponse.builder()
                .username(user.getCredential().getUsername())
                .name(user.getName())
                .email(user.getCredential().getEmail())
                .access(roles)
                .build();
    }

    private Specification<User> getManageUserSpesification(SearchManageUserRequest request, User user, List<Credential> credentials) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (!credentials.isEmpty()) {
                predicates.add(root.get("credential").in(credentials));
            }

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

