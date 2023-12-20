package com.danamon.autochain.config;

import com.danamon.autochain.constant.ActorType;
import com.danamon.autochain.constant.RoleType;
import com.danamon.autochain.entity.*;
import com.danamon.autochain.repository.*;
import com.danamon.autochain.security.BCryptUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SeederConfiguration implements CommandLineRunner {
    private final CredentialRepository credentialRepository;
    private final BackOfficeRepository backOfficeRepository;
    private final CompanyRepository companyRepository;
    private final RolesRepository rolesRepository;
    private final UserRolesRepository userRolesRepository;
    private final BCryptUtil bCryptUtil;

    private final String email = "wiryamn08@gmail.com";
    private final String userame= "Wirya";
    private final String password = "string";

    @Override
    public void run(String... args) {
        Optional<Credential> byUsername = credentialRepository.findByEmail(email);
        if(byUsername.isEmpty()){
            rolesSeeder();
            companySeeder();
            backofficeSeeder();
        }
    }

    public void backofficeSeeder(){

        Roles superadmin = rolesRepository.findByRoleName("SUPER_ADMIN").orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "roles not exist"));

        UserRole userRole = UserRole.builder()
                .role(superadmin)
                .build();

        List<UserRole> role = new ArrayList<>();
        // Create and save seed data for Credential entity
        Credential adminCredential = new Credential();
        adminCredential.setCredentialId("1");
        adminCredential.setEmail(email);
        adminCredential.setUsername(userame);
        adminCredential.setPassword(bCryptUtil.hashPassword(password));
        adminCredential.setActor(ActorType.BACKOFFICE);
        adminCredential.setRoles(role);
        adminCredential.setModifiedDate(LocalDateTime.now());
        adminCredential.setCreatedDate(LocalDateTime.now());
        adminCredential.setCreatedBy(userame);
        adminCredential.setModifiedBy(userame);

        BackOffice backOffice = new BackOffice();
        backOffice.setCredential(adminCredential);

        role.add(
                UserRole.builder()
                        .role(superadmin)
                        .credential(adminCredential)
                        .build()
        );

//        userRolesRepository.saveAllAndFlush(role);
        credentialRepository.saveAndFlush(adminCredential);
        backOfficeRepository.saveAndFlush(backOffice);
    }

    public void companySeeder(){
        Company company = new Company();
        company.setCompany_id("0");
        company.setCompanyName("string");
        company.setCompanyEmail("string");
        company.setCity("string");
        company.setAddress("string");
        company.setAccountNumber("string");
        company.setFinancingLimit(12313d);
        company.setRemainingLimit(123513d);
        company.setPhoneNumber("string");
        company.setProvince("string");

        companyRepository.saveAndFlush(company);
    }

    public void rolesSeeder(){
        List<Roles> allRole = List.of(
//                ====================== BACK OFFICE ROLE ===============
                Roles.builder().roleName(RoleType.SUPER_ADMIN.getName()).build(),
                Roles.builder().roleName(RoleType.ADMIN.getName()).build(),
                Roles.builder().roleName(RoleType.RELATIONSHIP_MANAGER.getName()).build(),
                Roles.builder().roleName(RoleType.CREDIT_ANALYST.getName()).build(),
//                ========================= USER ROLE ====================
                Roles.builder().roleName(RoleType.SUPER_USER.getName()).build(),
                Roles.builder().roleName(RoleType.FINANCE_STAFF.getName()).build(),
                Roles.builder().roleName(RoleType.INVOICE_STAFF.getName()).build(),
                Roles.builder().roleName(RoleType.PAYMENT_STAFF.getName()).build()
        );
        rolesRepository.saveAllAndFlush(allRole);
    }
}
