package com.danamon.autochain.config;

import com.danamon.autochain.constant.*;
import com.danamon.autochain.constant.invoice.ProcessingStatusType;
import com.danamon.autochain.constant.invoice.InvoiceStatus;
import com.danamon.autochain.constant.payment.PaymentMethod;
import com.danamon.autochain.constant.payment.PaymentStatus;
import com.danamon.autochain.constant.payment.PaymentType;
import com.danamon.autochain.entity.*;
import com.danamon.autochain.repository.*;
import com.danamon.autochain.security.BCryptUtil;
import com.danamon.autochain.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
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
    private final CompanyService companyService;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final BCryptUtil bCryptUtil;

//    =================== BACKOFFICE ACCOUNT as SUPER ADMIN ====================
    private final String bo_email = "rizdaagisa99@gmail.com";
    private final String bo_username = "rizda backoffice";
    private final String bo_password = "string";

//    ====================== USER ACCOUNT as SUPER USER =========================
    private final String user_email = "rizdaagisa@gmail.com";
    private final String user_username = "rizda user";
    private final String user_password = "string";

    @Override
    public void run(String... args) {
        Optional<Credential> byUsername = credentialRepository.findByEmail(bo_email);
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
        adminCredential.setEmail(bo_email);
        adminCredential.setUsername(bo_username);
        adminCredential.setPassword(bCryptUtil.hashPassword(bo_password));
        adminCredential.setActor(ActorType.BACKOFFICE);
        adminCredential.setRoles(role);
        adminCredential.setModifiedDate(LocalDateTime.now());
        adminCredential.setCreatedDate(LocalDateTime.now());
        adminCredential.setCreatedBy(bo_username);
        adminCredential.setModifiedBy(bo_username);

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
        Roles finance = rolesRepository.findByRoleName("FINANCE_STAFF").orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "roles not exist"));
        Roles invoice = rolesRepository.findByRoleName("INVOICE_STAFF").orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "roles not exist"));
        Company company = companyRepository.findBycompanyName("PT. Enigma").orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "company ID not found"));

        //        ================================= SUPER USER  ================================

        List<UserRole> roleUser = new ArrayList<>();

        Credential userCredential = Credential.builder()
                .email(user_email)
                .username(user_username)
                .password(bCryptUtil.hashPassword(user_password))
                .actor(ActorType.USER)
                .roles(roleUser)
                .modifiedDate(LocalDateTime.now())
                .createdDate(LocalDateTime.now())
                .createdBy(user_username)
                .modifiedBy(user_username)
                .build();

        roleUser.add(
                UserRole.builder()
                        .role(superUser)
                        .credential(userCredential)
                        .build()
        );

        credentialRepository.saveAndFlush(userCredential);

        User user = new User();
        user.setCompany(company);
        user.setCredential(userCredential);
        user.setName(user_username);

        userRepository.saveAndFlush(user);

        //        ================================= USER 2 ================================

        Company company2 = companyRepository.findBycompanyName("PT. Camp").orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "company name not found"));

        List<UserRole> roleUser2 = new ArrayList<>();
        Credential userCredential2 = Credential.builder()
                .email("root2@gmail.com")
                .username("oreo")
                .password(bCryptUtil.hashPassword("string"))
                .actor(ActorType.USER)
                .roles(roleUser2)
                .modifiedDate(LocalDateTime.now())
                .createdDate(LocalDateTime.now())
                .createdBy("oreo")
                .modifiedBy("oreo")
                .build();

        roleUser2.add(
                UserRole.builder()
                        .role(superUser)
                        .credential(userCredential2)
                        .build()
        );

        credentialRepository.saveAndFlush(userCredential2);

        User user2 = new User();
        user2.setCompany(company2);
        user2.setCredential(userCredential2);
        user2.setName("root2");

        userRepository.saveAndFlush(user2);

        //        ================================= USER 3 ================================

        List<UserAccsess> userAccsesses = new ArrayList<>();

        List<UserRole> roleUser3 = new ArrayList<>();
        Credential userCredential3 = Credential.builder()
                .email("root3@gmail.com")
                .username("oreo")
                .password(bCryptUtil.hashPassword("string"))
                .actor(ActorType.USER)
                .roles(roleUser3)
                .modifiedDate(LocalDateTime.now())
                .createdDate(LocalDateTime.now())
                .createdBy("oreo")
                .modifiedBy("oreo")
                .build();

        roleUser3.add(
                UserRole.builder()
                        .role(finance)
                        .credential(userCredential3)
                        .build()
        );

        roleUser3.add(
                UserRole.builder()
                        .role(invoice)
                        .credential(userCredential3)
                        .build()
        );

        credentialRepository.saveAndFlush(userCredential3);

        User user3 = new User();
        user3.setCompany(company2);
        user3.setCredential(userCredential3);
        user3.setUserAccsess(userAccsesses);
        user3.setName("root3");

        userAccsesses.add(
                UserAccsess.builder()
                        .company(company)
                        .user(user3)
                        .build()
        );

        userRepository.saveAndFlush(user3);

        //        ================================= USER 4 ================================

        Company company3 = companyRepository.findBycompanyName("PT. Toyota").orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "company name not found"));

        List<UserRole> roleUser4 = new ArrayList<>();
        Credential userCredential4 = Credential.builder()
                .email("toyota@gmail.com")
                .username("toy")
                .password(bCryptUtil.hashPassword("string"))
                .actor(ActorType.USER)
                .roles(roleUser4)
                .modifiedDate(LocalDateTime.now())
                .createdDate(LocalDateTime.now())
                .createdBy("oreo")
                .modifiedBy("oreo")
                .build();

        roleUser4.add(
                UserRole.builder()
                        .role(superUser)
                        .credential(userCredential4)
                        .build()
        );

        credentialRepository.saveAndFlush(userCredential4);

        User user4 = new User();
        user4.setCompany(company2);
        user4.setCredential(userCredential4);
        user4.setName("toyota");

        userRepository.saveAndFlush(user4);
    }

    public void companySeeder() {
        Company company = new Company();
        company.setCompany_id("ROO123");
        company.setCompanyName("PT. Enigma");
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
        company2.setCompanyName("PT. Camp");
        company2.setCompanyEmail("root2");
        company2.setCity("root");
        company2.setAddress("root");
        company2.setAccountNumber("root");
        company2.setFinancingLimit(12313d);
        company2.setRemainingLimit(123513d);
        company2.setPhoneNumber("root");
        company2.setProvince("root");

        companyRepository.saveAndFlush(company2);

        Company company3 = new Company();
        company3.setCompany_id("ROO456");
        company3.setCompanyName("PT. Toyota");
        company3.setCompanyEmail("toyota");
        company3.setCity("root");
        company3.setAddress("root");
        company3.setAccountNumber("root");
        company3.setFinancingLimit(12313d);
        company3.setRemainingLimit(123513d);
        company3.setPhoneNumber("root");
        company3.setProvince("root");

        companyRepository.saveAndFlush(company3);
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
        Optional<Credential> byUsername = credentialRepository.findByEmail(bo_email);

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
                .requestedBy(byUsername.get())
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

//        ============= WAITING STATUS INVOICE ========
        Invoice wait_invoice = Invoice.builder()
                .senderId(partner)
                .recipientId(company)
                .dueDate(yesterday)
                .invoiceStatus(InvoiceStatus.UNPAID)
                .status(ProcessingStatusType.WAITING_STATUS)
                .amount(500000L)
                .createdDate(LocalDateTime.now())
                .createdBy(null)
                .itemList("[{\"itemsName\" : \"Spion Astut\", \"itemsQuantity\" : 10, \"unitPrice\" : 10000},{\"itemsName\" : \"Ketut\", \"itemsQuantity\" : 10, \"unitPrice\" : 20000}]")
                .build();

        invoiceRepository.saveAndFlush(wait_invoice);


        Invoice invoice = Invoice.builder()
                .senderId(company)
                .recipientId(partner)
                .dueDate(yesterday)
                .invoiceStatus(InvoiceStatus.UNPAID)
                .status(ProcessingStatusType.APPROVE_INVOICE)
                .amount(100000L)
                .createdDate(LocalDateTime.now())
                .createdBy(null)
                .itemList("[{\"itemsName\" : \"Spion Astut\", \"itemsQuantity\" : 10, \"unitPrice\" : 10000},{\"itemsName\" : \"Ketut\", \"itemsQuantity\" : 10, \"unitPrice\" : 20000}]")
                .build();

        Invoice savedInvoice1 = invoiceRepository.saveAndFlush(invoice);

        Payment payment = Payment.builder()
                .senderId(company)
                .recipientId(partner)
                .invoice(savedInvoice1)
                .amount(100000L)
                .type(PaymentType.INVOICING)
                .dueDate(new Date())
                .paidDate(new Date())
                .method(PaymentMethod.BANK_TRANSFER)
                .status(PaymentStatus.UNPAID)
                .build();

        paymentRepository.saveAndFlush(payment);

        Invoice invoice2 = Invoice.builder()
                .senderId(company)
                .recipientId(partner)
                .dueDate(yesterday)
                .invoiceStatus(InvoiceStatus.PAID)
                .status(ProcessingStatusType.APPROVE_INVOICE)
                .amount(300000L)
                .createdDate(LocalDateTime.now())
                .createdBy(null)
                .itemList("[{\"itemsName\" : \"Spion Astut\", \"itemsQuantity\" : 10, \"unitPrice\" : 10000},{\"itemsName\" : \"Ketut\", \"itemsQuantity\" : 10, \"unitPrice\" : 20000}]")
                .build();

        Invoice savedInvoice2 = invoiceRepository.saveAndFlush(invoice2);

        Payment payment2 = Payment.builder()
                .senderId(company)
                .recipientId(partner)
                .invoice(savedInvoice2)
                .amount(300000L)
                .type(PaymentType.INVOICING)
                .dueDate(new Date())
                .paidDate(new Date())
                .method(PaymentMethod.BANK_TRANSFER)
                .status(PaymentStatus.UNPAID)
                .build();

        paymentRepository.saveAndFlush(payment2);
    }
}
