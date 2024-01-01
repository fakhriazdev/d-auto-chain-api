package com.danamon.autochain.config;

import com.danamon.autochain.constant.*;
import com.danamon.autochain.constant.invoice.ProcessingStatusType;
import com.danamon.autochain.constant.invoice.Status;
import com.danamon.autochain.entity.*;
import com.danamon.autochain.repository.*;
import com.danamon.autochain.security.BCryptUtil;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.service.PartnershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class SeederConfiguration implements CommandLineRunner {
    private final CredentialRepository credentialRepository;
    private final BackOfficeRepository backOfficeRepository;
    private final CompanyRepository companyRepository;
    private final RolesRepository rolesRepository;
    private final UserRepository userRepository;
    private final PartnershipRepository partnershipRepository;
    private final UserRolesRepository userRolesRepository;
    private final CompanyService companyService;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final BCryptUtil bCryptUtil;

    private final String email = "erwinperdana2@gmail.com";
    private final String username = "rizda";
    private final String password = "string";

    @Override
    public void run(String... args) {
        Optional<Credential> byUsername = credentialRepository.findByEmail(email);
        if (byUsername.isEmpty()) {
            rolesSeeder();
            companySeeder();
            backofficeSeeder();
            userSeeder();
            partnershipSeeder();
            invoiceAndPaymentSeeder();
        }
    }

    public void backofficeSeeder() {
        Roles superadmin = rolesRepository.findByRoleName("SUPER_ADMIN").orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "roles not exist"));

        List<UserRole> role = new ArrayList<>();
        // Create and save seed data for Credential entity
        Credential adminCredential = new Credential();
        adminCredential.setEmail(email);
        adminCredential.setUsername(username);
        adminCredential.setPassword(bCryptUtil.hashPassword(password));
        adminCredential.setActor(ActorType.BACKOFFICE);
        adminCredential.setRoles(role);
        adminCredential.setModifiedDate(LocalDateTime.now());
        adminCredential.setCreatedDate(LocalDateTime.now());
        adminCredential.setCreatedBy(username);
        adminCredential.setModifiedBy(username);

        BackOffice backOffice = new BackOffice();
        backOffice.setCredential(adminCredential);

        role.add(
                UserRole.builder()
                        .role(superadmin)
                        .credential(adminCredential)
                        .build()
        );

        credentialRepository.saveAndFlush(adminCredential);
        backOfficeRepository.saveAndFlush(backOffice);
    }

    public void userSeeder(){
        Roles superUser = rolesRepository.findByRoleName("SUPER_USER").orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "roles not exist"));
        Company company = companyRepository.findBycompanyName("PT. Root").orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "company ID not found"));

        Credential userCredential = Credential.builder()
                .email("oreofinalprojectdtt@gmail.com")
                .username("oreo")
                .password(bCryptUtil.hashPassword("test12345"))
                .actor(ActorType.BACKOFFICE)
                .modifiedDate(LocalDateTime.now())
                .createdDate(LocalDateTime.now())
                .createdBy("oreo")
                .modifiedBy("oreo")
                .build();

        credentialRepository.saveAndFlush(userCredential);

        User user = new User();
        user.setCompany(company);
        user.setCredential(userCredential);

        userRepository.saveAndFlush(user);

        List<UserRole> roleUser = new ArrayList<>();
        roleUser.add(
                UserRole.builder()
                        .role(superUser)
                        .credential(userCredential)
                        .build()
        );

        userCredential.setRoles(roleUser);

        Company company2 = companyRepository.findBycompanyName("PT. Root2").orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "company name not found"));

        Credential userCredential2 = Credential.builder()
                .email("root2@gmail.com")
                .username("oreo")
                .password(bCryptUtil.hashPassword("string"))
                .actor(ActorType.BACKOFFICE)
                .modifiedDate(LocalDateTime.now())
                .createdDate(LocalDateTime.now())
                .createdBy("oreo")
                .modifiedBy("oreo")
                .build();

        credentialRepository.saveAndFlush(userCredential2);

        User user2 = new User();
        user.setCompany(company2);
        user.setCredential(userCredential2);

        userRepository.saveAndFlush(user2);

        List<UserRole> roleUser2 = new ArrayList<>();
        roleUser.add(
                UserRole.builder()
                        .role(superUser)
                        .credential(userCredential2)
                        .build()
        );

        userCredential.setRoles(roleUser2);
    }

    public void companySeeder() {
        Company company = new Company();
        company.setCompany_id("ROO123");
        company.setCompanyName("PT. Root");
        company.setCompanyEmail("root");
        company.setCity("root");
        company.setAddress("root");
        company.setAccountNumber("root");
        company.setFinancingLimit(12313d);
        company.setRemainingLimit(123513d);
        company.setPhoneNumber("root");
        company.setProvince("root");

        companyRepository.saveAndFlush(company);

        Company company2 = new Company();
        company2.setCompany_id("ROO321");
        company2.setCompanyName("PT. Root2");
        company2.setCompanyEmail("root2");
        company2.setCity("root");
        company2.setAddress("root");
        company2.setAccountNumber("root");
        company2.setFinancingLimit(12313d);
        company2.setRemainingLimit(123513d);
        company2.setPhoneNumber("root");
        company2.setProvince("root");

        companyRepository.saveAndFlush(company2);
    }

    public void rolesSeeder() {
        List<Roles> roles = rolesRepository.findAll();

        if (roles.isEmpty()) {
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

    private void partnershipSeeder() {
        Company company = companyService.getById("ROO123");
        Company partner = companyService.getById("ROO321");

        String id = "CP-ROO123-ROO321";

        System.out.println();
        Partnership partnership = Partnership.builder()
                .partnershipNo(id)
                .company(company)
                .partner(partner)
                .partnerStatus(PartnershipStatus.IN_PARTNER)
                .partnerRequestedDate(LocalDateTime.now())
                .partnerConfirmationDate(null)
                .requestedBy(null)
                .confirmedBy(null)
                .build();

        partnershipRepository.saveAndFlush(partnership);
    }

    private void invoiceAndPaymentSeeder() {
        Company company = companyService.getById("ROO123");
        Company partner = companyService.getById("ROO321");

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date yesterday = cal.getTime();

        Invoice invoice = Invoice.builder()
                .senderId(company)
                .recipientId(partner)
                .dueDate(yesterday)
                .status(Status.UNPAID)
                .processingStatus(ProcessingStatusType.APPROVE_INVOICE)
                .amount(100000L)
                .createdDate(LocalDateTime.now())
                .createdBy(null)
                .itemList("[{\"itemsName\" : \"Spion Astut\", \"itemsQuantity\" : 10, \"unitPrice\" : 10000},{\"itemsName\" : \"Ketut\", \"itemsQuantity\" : 10, \"unitPrice\" : 20000}]")
                .build();

        Invoice savedInvoice1 = invoiceRepository.saveAndFlush(invoice);

        Payment payment = Payment.builder()
                .invoice(savedInvoice1)
                .financingPayable(null)
                .amount(100000L)
                .type(PaymentType.INVOICING)
                .dueDate("2024-01-31T21:29:04.48")
                .paidDate(null)
                .method(PaymentMethod.BANK_TRANSFER)
                .source(null)
                .outstandingFlag(Status.UNPAID)
                .build();

        paymentRepository.saveAndFlush(payment);

        Invoice invoice2 = Invoice.builder()
                .senderId(company)
                .recipientId(partner)
                .dueDate(yesterday)
                .status(Status.PAID)
                .processingStatus(ProcessingStatusType.APPROVE_INVOICE)
                .amount(300000L)
                .createdDate(LocalDateTime.now())
                .createdBy(null)
                .itemList("[{\"itemsName\" : \"Spion Astut\", \"itemsQuantity\" : 10, \"unitPrice\" : 10000},{\"itemsName\" : \"Ketut\", \"itemsQuantity\" : 10, \"unitPrice\" : 20000}]")
                .build();

        Invoice savedInvoice2 = invoiceRepository.saveAndFlush(invoice2);

        Payment payment2 = Payment.builder()
                .invoice(savedInvoice2)
                .financingPayable(null)
                .amount(300000L)
                .type(PaymentType.INVOICING)
                .dueDate("2024-01-31T21:29:04.48")
                .paidDate(null)
                .method(PaymentMethod.BANK_TRANSFER)
                .source(null)
                .outstandingFlag(Status.PAID)
                .build();

        paymentRepository.saveAndFlush(payment2);
    }
}
