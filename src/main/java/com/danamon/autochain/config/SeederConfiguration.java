package com.danamon.autochain.config;

import com.danamon.autochain.constant.*;
import com.danamon.autochain.constant.financing.FinancingStatus;
import com.danamon.autochain.constant.financing.FinancingType;
import com.danamon.autochain.constant.invoice.ProcessingStatusType;
import com.danamon.autochain.constant.invoice.InvoiceStatus;
import com.danamon.autochain.constant.payment.PaymentMethod;
import com.danamon.autochain.constant.payment.PaymentStatus;
import com.danamon.autochain.constant.payment.PaymentType;
import com.danamon.autochain.entity.*;
import com.danamon.autochain.repository.*;
import com.danamon.autochain.security.BCryptUtil;
import com.danamon.autochain.service.CompanyService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.management.relation.Role;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

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
    private final FinancingPayableRepository financingPayableRepository;
    private final FinancingReceivableRepository financingReceivableRepository;
    private final TenureRepository tenureRepository;
    private final BCryptUtil bCryptUtil;
    private final UserRolesRepository userRolesRepository;
    private final BackofficeAccessRepository backofficeAccessRepository;

    //    =================== BACKOFFICE ACCOUNT as SUPER ADMIN ====================
    private final String bo_email = "oreofinalprojectdtt@gmail.com";
    private final String bo_username = "oreofinalprojectdtt backoffice";
    private final String bo_password = "string";

    //    ====================== USER ACCOUNT as SUPER USER =========================
    private final String user_email = "tambunanferdinand1@gmail.com";
    private final String user_username = "tambunanferdinand1 user";
    private final String user_password = "string";

    @Override
    public void run(String... args) {
        Optional<Credential> byUsername = credentialRepository.findByEmail(bo_email);
        if (byUsername.isEmpty()) {
            rolesSeeder();
            superBackofficeSeeder();
            companySeeder();
            superUserSeeder();
//
            newBackOfficeSeeder();
            newUserSeeder();
//
////            userSeeder();
            invoiceSeeder();
            paymentSeeder();

            financingSeeder();
////            invoiceAndPaymentSeeder();
        }
    }

    private void superUserSeeder() {
        createUser(user_username, user_username, user_email, "AST123", List.of("IND234", "ITA567"), List.of("SUPER_USER"));
    }

    public void superBackofficeSeeder() {
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

    public void userSeeder() {
        Roles superUser = rolesRepository.findByRoleName("SUPER_USER").orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "roles not exist"));
        Roles finance = rolesRepository.findByRoleName("FINANCE_STAFF").orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "roles not exist"));
        Roles invoice = rolesRepository.findByRoleName("INVOICE_STAFF").orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "roles not exist"));
        Company company = companyRepository.findById("AST123").orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "company ID not found"));

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
        user4.setCompany(company3);
        user4.setCredential(userCredential4);
        user4.setName("toyota");

        userRepository.saveAndFlush(user4);
    }

    public void companySeeder() {
        Roles superUser = rolesRepository.findByRoleName("SUPER_USER").orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "roles not exist"));

        createCompany("AST123", "PT. Astra Otopart Tbk", "supplychainfinanceadmin@astraotopart.co.id", "Jakarta Utara", "DKI Jakarta",
                "Jl. Raya Pegangsaan Dua Km. 2,2", "010101001", 2000000000d, 2000000000d, "082123456780",
                "supplychainfinanceadmin@astraotopart.co.id", "scfadminotopart", superUser);

        createCompany("GAJ456", "PT. Gajah Tunggal Tbk", "scfgajahtunggal@gajahtunggal.co.id", "Karawang", "Jawa Barat",
                "Jl. Trans Heksa, Kawasan Industri KJIE, Wanasari", "010101002", 2000000000d, 2000000000d, "082123456781",
                "scfgajahtunggal@gajahtunggal.co.id", "scfadmingajahtunggal", superUser);

        createCompany("ADI789", "PT. Adira Finance tbk", "adirafinancescf@adira.co.id", "Jakarta Selatan", "DKI Jakarta",
                "Jl. Jenderal Sudirman No.Kav.25 12, RT.12/RW.1, Kuningan", "010101003", 5000000000d, 5000000000d, "082123456782",
                "adirafinancescf@adira.co.id", "scfadminadira", superUser);

        createCompany("IND234", "PT. Indospring Tbk", "indospringscfadmin@indospring.co.id", "Kabupaten Bekasi", "Jawa Barat",
                "Jl. Kalibaru Timur No.10, RT.10/RW.15, Kali Baru", "010101004", 1500000000d, 1500000000d, "082123456783",
                "indospringscfadmin@indospring.co.id", "scfadminindospring", superUser);

        createCompany("ITA567", "PT. Itama Ranoraya Tbk", "supplychainfinancingadmin@itama.co.id", "Jakarta Selatan", "DKI Jakarta",
                "Jl. Raya Pasar Minggu No.18 21st Floor, RT.1/RW.1, Pejaten Timur, Ps. Minggu", "010101005", 2000000000d, 2000000000d, "082123456784",
                "supplychainfinancingadmin@itama.co.id", "scfadmin_itama", superUser);

        createCompany("MUL890", "PT. Multistrada Arah Sarana Tbk", "financeadmin@multistrada.co.id", "Jakarta Selatan", "DKI Jakarta",
                "Michelin Indonesia, RT.4/RW.3, Pondok Indah", "010101006", 2500000000d, 2500000000d, "082123456785",
                "financeadmin@multistrada.co.id", "scfadmin_multistrada", superUser);

        createCompany("GAR123", "PT Garuda Metalindo Tbk", "sc_finance@garudametalindo.co.id", "Jakarta Utara", "DKI Jakarta",
                "Jl. Kamal Muara No.23, RT.2/RW.2, Kamal Muara", "010101007", 3000000000d, 3000000000d, "082123456786",
                "sc_finance@garudametalindo.co.id", "scfadmin_metalindo", superUser);

        createCompany("UNI456", "PT United Tractors Tbk", "unitedtractors.scfadmin@ut.co.id", "Jakarta Timur", "DKI Jakarta",
                "Jl. Raya Bekasi No.KM.22, RT.7/RW.1, Cakung Bar.", "010101008", 4000000000d, 4000000000d, "082123456787",
                "unitedtractors.scfadmin@ut.co.id", "scfadmin.ut", superUser);

        createCompany("GAY789", "PT. Gaya Makmur Tractors", "financeadmin@gayamakmur.co.id", "Jakarta Barat", "DKI Jakarta",
                "Jl. Lingkar Luar Barat No.3 Rawa Buaya", "010101009", 3000000000d, 3000000000d, "082123456788",
                "financeadmin@gayamakmur.co.id", "scfadmin_gayamakmur", superUser);

        createCompany("SEL234", "PT. Selamat Sempurna Tbk", "admin.finance@selamat.co.id", "Jakarta Utara", "DKI Jakarta",
                "Jl. Pluit Raya I No. 1", "010101010", 2000000000d, 2000000000d, "082123456789",
                "admin.finance@selamat.co.id", "scfadmin_selamatsempurna", superUser);

        createCompany("REL567", "PT. Relindo Multi Traktor Tbk", "scf_admin@relindo.co.id", "Tangerang", "Banten",
                "Rukan Crown, block C, Jl. Green Lake City Boulevard No.8, RT.004/RW.008", "010101011", 2500000000d, 2500000000d, "082123456790",
                "scf_admin@relindo.co.id", "scfadmin.relindo", superUser);

        createCompany("DWI890", "PT. Dwimitra Sejahtera BersamaTbk", "admin.scf@dwimitra.co.id", "Jakarta Utara", "DKI Jakarta",
                "Jl. Pemandangan III No.5B, RT.1/RW.1, Pademangan Bar.", "010101012", 1500000000d, 1500000000d, "082123456791",
                "admin.scf@dwimitra.co.id", "scfadmin_dwimitra", superUser);

        createCompany("IND345", "PT Indomobil SuksesTbk", "financeadmin@indomobil.co.id", "Jakarta Timur", "DKI Jakarta",
                "Wisma Indomobil, Jl. Letjen M.T. Haryono No.Kav 8 1 Lt.6, Bidara Cina, Kecamatan Jatinegara", "010101013", 1800000000d, 1800000000d, "082123456791",
                "financeadmin@indomobil.co.id", "scfadmin_indomobil", superUser);

        partnershipSeeder();
    }

    private void createCompany(String companyId, String companyName, String companyEmail, String city, String province,
                               String address, String accountNumber, Double financingLimit, Double remainingLimit,
                               String phoneNumber, String userEmail, String username, Roles superUser) {
        Company company = new Company();
        company.setCompany_id(companyId);
        company.setCompanyName(companyName);
        company.setCompanyEmail(companyEmail);
        company.setCity(city);
        company.setProvince(province);
        company.setAddress(address);
        company.setAccountNumber(accountNumber);
        company.setFinancingLimit(financingLimit);
        company.setRemainingLimit(remainingLimit);
        company.setPhoneNumber(phoneNumber);

        companyRepository.saveAndFlush(company);

        List<UserRole> roleUser = new ArrayList<>();
        Credential userCredential = Credential.builder()
                .email(userEmail)
                .username(username)
                .password(bCryptUtil.hashPassword("oreo123"))
                .actor(ActorType.USER)
                .roles(roleUser)
                .modifiedDate(LocalDateTime.now())
                .createdDate(LocalDateTime.now())
                .createdBy(username)
                .modifiedBy(username)
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
        user.setName(username);

        userRepository.saveAndFlush(user);
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

        Company company = companyService.getById("AST123");
        Company CompanyPartner = companyService.getById("IND345");

        String id = "CP-AST123-IND345";

        Partnership partnership = Partnership.builder()
                .partnershipNo(id)
                .company(company)
                .partner(CompanyPartner)
                .partnerStatus(PartnershipStatus.IN_PARTNER)
                .partnerRequestedDate(LocalDateTime.now())
                .partnerConfirmationDate(null)
                .requestedBy(byUsername.get())
                .confirmedBy(null)
                .build();

        partnershipRepository.saveAndFlush(partnership);

        Company CompanyPartner2 = companyService.getById("ITA567");

        id = "CP-AST123-ITA567";

        Partnership partnership2 = Partnership.builder()
                .partnershipNo(id)
                .company(company)
                .partner(CompanyPartner2)
                .partnerStatus(PartnershipStatus.IN_PARTNER)
                .partnerRequestedDate(LocalDateTime.now())
                .partnerConfirmationDate(null)
                .requestedBy(byUsername.get())
                .confirmedBy(null)
                .build();

        partnershipRepository.saveAndFlush(partnership2);

        Company CompanyPartner3 = companyService.getById("GAR123");

        id = "CP-AST123-GAR123";

        Partnership partnership3 = Partnership.builder()
                .partnershipNo(id)
                .company(company)
                .partner(CompanyPartner3)
                .partnerStatus(PartnershipStatus.IN_PARTNER)
                .partnerRequestedDate(LocalDateTime.now())
                .partnerConfirmationDate(null)
                .requestedBy(byUsername.get())
                .confirmedBy(null)
                .build();

        partnershipRepository.saveAndFlush(partnership3);

        //=======================================================

        Company company2 = companyService.getById("GAJ456");
        Company Company2Partner = companyService.getById("IND234");

        id = "CP-GAJ456-IND234";

        Partnership partnership4 = Partnership.builder()
                .partnershipNo(id)
                .company(company2)
                .partner(Company2Partner)
                .partnerStatus(PartnershipStatus.IN_PARTNER)
                .partnerRequestedDate(LocalDateTime.now())
                .partnerConfirmationDate(null)
                .requestedBy(byUsername.get())
                .confirmedBy(null)
                .build();

        partnershipRepository.saveAndFlush(partnership4);

        Company Company2Partner2 = companyService.getById("MUL890");

        id = "CP-GAJ456-MUL890";

        Partnership partnership5 = Partnership.builder()
                .partnershipNo(id)
                .company(company2)
                .partner(Company2Partner2)
                .partnerStatus(PartnershipStatus.IN_PARTNER)
                .partnerRequestedDate(LocalDateTime.now())
                .partnerConfirmationDate(null)
                .requestedBy(byUsername.get())
                .confirmedBy(null)
                .build();

        partnershipRepository.saveAndFlush(partnership5);

        //=======================================================

        Company company3 = companyService.getById("REL567");
        Company Company3Partner = companyService.getById("UNI456");

        id = "CP-REL567-UNI456";

        Partnership partnership6 = Partnership.builder()
                .partnershipNo(id)
                .company(company3)
                .partner(Company3Partner)
                .partnerStatus(PartnershipStatus.IN_PARTNER)
                .partnerRequestedDate(LocalDateTime.now())
                .partnerConfirmationDate(null)
                .requestedBy(byUsername.get())
                .confirmedBy(null)
                .build();

        partnershipRepository.saveAndFlush(partnership6);

        Company Company3Partner2 = companyService.getById("SEL234");

        id = "CP-REL567-SEL234";

        Partnership partnership7 = Partnership.builder()
                .partnershipNo(id)
                .company(company3)
                .partner(Company3Partner2)
                .partnerStatus(PartnershipStatus.IN_PARTNER)
                .partnerRequestedDate(LocalDateTime.now())
                .partnerConfirmationDate(null)
                .requestedBy(byUsername.get())
                .confirmedBy(null)
                .build();

        partnershipRepository.saveAndFlush(partnership7);

        Company Company3Partner3 = companyService.getById("GAR123");

        id = "CP-REL567-GAR123";

        Partnership partnership8 = Partnership.builder()
                .partnershipNo(id)
                .company(company3)
                .partner(Company3Partner3)
                .partnerStatus(PartnershipStatus.IN_PARTNER)
                .partnerRequestedDate(LocalDateTime.now())
                .partnerConfirmationDate(null)
                .requestedBy(byUsername.get())
                .confirmedBy(null)
                .build();

        partnershipRepository.saveAndFlush(partnership8);

        //=======================================================

        Company company4 = companyService.getById("GAR123");
        Company Company4Partner = companyService.getById("DWI890");

        id = "CP-GAR123-DWI890";

        Partnership partnership9 = Partnership.builder()
                .partnershipNo(id)
                .company(company4)
                .partner(Company4Partner)
                .partnerStatus(PartnershipStatus.IN_PARTNER)
                .partnerRequestedDate(LocalDateTime.now())
                .partnerConfirmationDate(null)
                .requestedBy(byUsername.get())
                .confirmedBy(null)
                .build();

        partnershipRepository.saveAndFlush(partnership9);
    }

    private void invoiceAndPaymentSeeder() {
        Company company = companyService.getById("AST123");
        Company partner = companyService.getById("ITA567");

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date yesterday = cal.getTime();

//        ============= WAITING STATUS INVOICE ========
        Invoice wait_invoice = Invoice.builder()
                .senderId(partner)
                .recipientId(company)
                .dueDate(yesterday)
                .status(InvoiceStatus.UNPAID)
                .processingStatus(ProcessingStatusType.WAITING_STATUS)
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
                .status(InvoiceStatus.UNPAID)
                .processingStatus(ProcessingStatusType.APPROVE_INVOICE)
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
                .createdDate(new Date())
                .method(PaymentMethod.BANK_TRANSFER)
                .status(PaymentStatus.UNPAID)
                .build();

        paymentRepository.saveAndFlush(payment);

        Invoice invoice2 = Invoice.builder()
                .senderId(company)
                .recipientId(partner)
                .dueDate(yesterday)
                .status(InvoiceStatus.PAID)
                .processingStatus(ProcessingStatusType.APPROVE_INVOICE)
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
                .createdDate(new Date())
                .method(PaymentMethod.BANK_TRANSFER)
                .status(PaymentStatus.UNPAID)
                .build();

        paymentRepository.saveAndFlush(payment2);
    }

    private void createUser(String username, String name, String email, String company, List<String> handleCompany, List<String> role) {
        List<UserRole> userRoles = new ArrayList<>();

        Credential credential = Credential.builder()
                .createdDate(LocalDateTime.now())
                .email(email)
                .username(username)
                .roles(userRoles)
                .createdBy(username)
                .actor(ActorType.USER)
                .password(bCryptUtil.hashPassword("string"))
                .modifiedBy(username)
                .modifiedDate(LocalDateTime.now())
                .build();

        credentialRepository.saveAndFlush(credential);

        Company getCompany = companyRepository.findById(company).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Company Not Found"));

        List<UserAccsess> userAccsesses = new ArrayList<>();

        User user = User.builder()
                .company(getCompany)
                .credential(credential)
                .userAccsess(userAccsesses)
                .credential(credential)
                .name(name)
                .build();

        handleCompany.forEach(c -> {
            Company get = companyRepository.findById(c).orElseThrow(() -> new EntityNotFoundException("company not found"));
            userAccsesses.add(
                    UserAccsess.builder()
                            .user(user)
                            .company(get)
                            .build()
            );
        });
        role.forEach(r ->
                {
                    Roles roles = rolesRepository.findByRoleName(r).orElseThrow(() -> new EntityNotFoundException("role not found"));
                    userRoles.add(
                            UserRole.builder()
                                    .credential(credential)
                                    .role(roles)
                                    .build()
                    );
                }
        );

        userRepository.saveAndFlush(user);
        userRolesRepository.saveAllAndFlush(userRoles);
    }

    private void newUserSeeder() {
//        createUser("oreo_123", "Oreo Jaya", "oreofinalprojectdtt@gmail.com", "AST123", List.of("IND345", "ITA567"), List.of("INVOICE_STAFF", "FINANCE_STAFF"));

        createUser("nand_123", "Nand", "dinandtambunan28@gmail.com", "AST123", List.of("GAR123"), List.of("PAYMENT_STAFF"));

        createUser("abra_123", "Abraham", "abramyct@gmail.com", "AST123", List.of("GAR123"), List.of("PAYMENT_STAFF", "INVOICE_STAFF", "FINANCE_STAFF"));


        createUser("edia_123", "Ediashta", "ediashtarevin77@gmail.com", "GAR123", List.of("AST123"), List.of("PAYMENT_STAFF", "INVOICE_STAFF", "FINANCE_STAFF"));

        createUser("jere_123", "Jeremy", "jeremysilaban3@gmail.com", "GAR123", List.of("UNI456"), List.of("INVOICE_STAFF"));

        createUser("oliv_123", "Oliver", "olivermuhammadf@gmail.com", "GAR123", List.of("IND345"), List.of("PAYMENT_STAFF", "FINANCE_STAFF"));
    }

    private void createBackoffice(String username, String name, String email, String role, List<String> handleCompany) {
        List<UserRole> userRoles = new ArrayList<>();

        Credential credential = Credential.builder()
                .modifiedDate(LocalDateTime.now())
                .createdDate(LocalDateTime.now())
                .createdBy(username)
                .modifiedDate(LocalDateTime.now())
                .modifiedBy(username)
                .actor(ActorType.BACKOFFICE)
                .roles(userRoles)
                .email(email)
                .password(bCryptUtil.hashPassword("string"))
                .username(username)
                .build();

        Roles roles = rolesRepository.findByRoleName(role).orElseThrow(() -> new EntityNotFoundException("Role not found"));

        userRoles.add(
                UserRole.builder()
                        .role(roles)
                        .credential(credential)
                        .build()
        );


        BackOffice backOffice = BackOffice.builder()
                .name(name)
                .credential(credential)
                .build();

        credential.setBackOffice(backOffice);

        credentialRepository.saveAndFlush(credential);


        if (RoleType.RELATIONSHIP_MANAGER.getName().equals(roles.getRoleName()) && !handleCompany.isEmpty()) {
            List<BackofficeUserAccess> backofficeUserAccesses = new ArrayList<>();
            handleCompany.forEach(c ->
                    {
                        Company company = companyRepository.findById(c).orElseThrow(() -> new EntityNotFoundException("Company With ID " + c + " Not Found"));
                        backofficeUserAccesses.add(
                                BackofficeUserAccess.builder()
                                        .backOffice(backOffice)
                                        .company(company)
                                        .build()
                        );
                    }
            );
            backOffice.setBackofficeUserAccesses(backofficeUserAccesses);
        }

        backOfficeRepository.saveAndFlush(backOffice);
        userRolesRepository.saveAllAndFlush(userRoles);
    }

    private void newBackOfficeSeeder() {
//        createBackoffice("superadmin_1", "Super Admin A", "oreofinalprojectdtt2@gmail.com","SUPER_ADMIN", null);

        createBackoffice("admin_1", "Admin A", "admin@gmail.com", "ADMIN", null);

        createBackoffice("ca_1", "Credit Analyst A", "ca@gmail.com", "CREDIT_ANALYST", null);

        createBackoffice("rm_1", "Relation Manager A", "rm@gmail.com", "RELATIONSHIP_MANAGER", List.of("GAJ456"));
    }

    private void invoiceSeeder() {
        try {
            //        ================= COMPLETED ==============
            String item = "[{\"itemsName\" : \"Torque Assy Rod Truck\", \"itemsQuantity\" : 140, \"unitPrice\" : 450000},{\"itemsName\" : \"Fuel Tank Breather\", \"itemsQuantity\" : 160, \"unitPrice\" : 300000}, {\"itemsName\" : \"Passenger Air Bag Less Door\", \"itemsQuantity\" : 180, \"unitPrice\" : 500000}]";
            generateInvoice("INV-AST123-001", "IND345", "AST123", 201000000l, "16-08-2023", "15-06-2023", InvoiceStatus.UNPAID, ProcessingStatusType.WAITING_STATUS, item);

            String item2 = "[{\"itemsName\" : \"Car Battery\", \"itemsQuantity\" : 140, \"unitPrice\" : 500000},{\"itemsName\" : \"Car Horn\", \"itemsQuantity\" : 160, \"unitPrice\" : 650000}, {\"itemsName\" : \"Meter Cluster\", \"itemsQuantity\" : 200, \"unitPrice\" : 15000000}]";
            generateInvoice("INV-AST123-002", "ITA567", "AST123", 474000000l, "25-11-2023", "24-08-2023", InvoiceStatus.UNPAID, ProcessingStatusType.WAITING_STATUS, item2);

            String item3 = "[{\"itemsName\" : \"Gajah Tunggal Ban Luar Mobil\", \"itemsQuantity\" : 280, \"unitPrice\" : 450000},{\"itemsName\" : \"Gajah Tunggal Gt Radial Traction Pro\", \"itemsQuantity\" : 240, \"unitPrice\" : 450000}, {\"itemsName\" : \"Gajah Tunggal Ban Mobil Gtx Pro\", \"itemsQuantity\" : 300, \"unitPrice\" : 690000}]";
            generateInvoice("INV-GAJ456-001", "IND234", "GAJ456", 5274000000l, "08-11-2023", "11-07-2023", InvoiceStatus.UNPAID, ProcessingStatusType.WAITING_STATUS, item3);

            String item4 = "[{\"itemsName\" : \"Gajah Tunggal Gt Radial Savero At Plus\", \"itemsQuantity\" : 50, \"unitPrice\" : 1300000},{\"itemsName\" : \"Gajah Tunggal Gt Radial Traction Pro\", \"itemsQuantity\" : 180, \"unitPrice\" : 810000}]";
            generateInvoice("INV-GAJ456-002", "MUL890", "GAJ456", 210800000l, "21-12-2023", "20-10-2023", InvoiceStatus.UNPAID, ProcessingStatusType.WAITING_STATUS, item4);

//        ================= ONGOING ===============
            String item5 = "[{\"itemsName\" : \"Car Battery\", \"itemsQuantity\" : 140, \"unitPrice\" : 450000},{\"itemsName\" : \"Fuel Tank Breather\", \"itemsQuantity\" : 160, \"unitPrice\" : 300000}, {\"itemsName\" : \"Meter Cluster\", \"itemsQuantity\" : 200, \"unitPrice\" : 15000000}]";
            generateInvoice("INV-AST123-003", "GAR123", "AST123", 418000000l, "17-04-2024", "16-01-2024", InvoiceStatus.UNPAID, ProcessingStatusType.APPROVE_INVOICE, item5);

            String item6 = "[{\"itemsName\" : \"Blok silinder\", \"itemsQuantity\" : 280, \"unitPrice\" : 450000},{\"itemsName\" : \"Kanvas rem\", \"itemsQuantity\" : 180, \"unitPrice\" : 300000}]";
            generateInvoice("INV-REL567-001", "UNI456", "REL567", 180000000l, "17-03-2024", "16-01-2024", InvoiceStatus.UNPAID, ProcessingStatusType.APPROVE_INVOICE, item6);

            String item7 = "[{\"itemsName\" : \"Pin and Collars\", \"itemsQuantity\" : 140, \"unitPrice\" : 300000},{\"itemsName\" : \"Bolts\", \"itemsQuantity\" : 160, \"unitPrice\" : 150000}, {\"itemsName\" : \"Nuts\", \"itemsQuantity\" : 200, \"unitPrice\" : 200000}]";
            generateInvoice("INV-GAR123-001", "DWI890", "GAR123", 106000000l, "17-03-2024", "16-01-2024", InvoiceStatus.UNPAID, ProcessingStatusType.APPROVE_INVOICE, item7);

            String item8 = "[{\"itemsName\" : \"Radiator\", \"itemsQuantity\" : 100, \"unitPrice\" : 3400000},{\"itemsName\" : \"Penyaring Udara\", \"itemsQuantity\" : 210, \"unitPrice\" : 550000}]";
            generateInvoice("INV-SEL234-001", "REL567", "SEL234", 455000000l, "10-12-2024", "10-10-2024", InvoiceStatus.UNPAID, ProcessingStatusType.APPROVE_INVOICE, item8);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void generateInvoice(String id, String recipientId, String senderId, Long amount, String dueDate, String invoiceDate, InvoiceStatus status, ProcessingStatusType statusType, String item) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date1 = null;
        Instant instant = null;
        try {
            date1 = dateFormat.parse(dueDate);
            Date date2 = dateFormat.parse(invoiceDate);
            instant = date2.toInstant();
        } catch (ParseException e) {
            System.out.println("Error parsing date: " + e.getMessage());
        }

        Company recipient = companyRepository.findById(recipientId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found on seeder with name : " + recipientId));
        Company sender = companyRepository.findById(senderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found on seeder with name : " + senderId));

        invoiceRepository.saveAndFlush(
                Invoice.builder()
                        .invoiceId(id)
                        .recipientId(recipient)
                        .senderId(sender)
                        .amount(amount)
                        .dueDate(date1)
                        .createdBy(sender.getCompanyName())
                        .createdDate(instant.atZone(ZoneId.systemDefault()).toLocalDateTime())
                        .status(status)
                        .processingStatus(statusType)
                        .itemList(item)
                        .build()
        );
    }

    private void paymentSeeder() {
        try {
            //        ================================ PAYMENT COMPANY TO COMPANY ===============================
            generatePayment("PAY-IND345-001", "INV-AST123-001", "AST123", "IND345", PaymentType.INVOICING, PaymentStatus.UNPAID, PaymentMethod.BANK_TRANSFER, 201000000L, "16-08-2023", null);
            generatePayment("PAY-ITA567-001", "INV-AST123-002", "AST123", "ITA567", PaymentType.INVOICING, PaymentStatus.PAID, PaymentMethod.BANK_TRANSFER, 474000000L, "25-11-2023", "20-11-2023");
            generatePayment("PAY-IND234-001", "INV-GAJ456-001", "GAJ456", "IND234", PaymentType.INVOICING, PaymentStatus.LATE_PAID, PaymentMethod.BANK_TRANSFER, 527400000L, "08-11-2023", "19-01-2024");
            generatePayment("PAY-MUL890-001", "INV-GAJ456-002", "GAJ456", "MUL890", PaymentType.INVOICING, PaymentStatus.UNPAID, PaymentMethod.BANK_TRANSFER, 210800000L, "21-12-2023", null);
            generatePayment("PAY-GAR123-001", "INV-AST123-003", "AST123", "GAR123", PaymentType.INVOICING, PaymentStatus.UNPAID, PaymentMethod.BANK_TRANSFER, 418000000L, "17-04-2024", null);
            generatePayment("PAY-UNI456-001", "INV-REL567-001", "REL567", "UNI456", PaymentType.INVOICING, PaymentStatus.UNPAID, PaymentMethod.BANK_TRANSFER, 180000000L, "17-03-2024", null);
            generatePayment("PAY-DWI890-001", "INV-GAR123-001", "GAR123", "DWI890", PaymentType.INVOICING, PaymentStatus.UNPAID, PaymentMethod.BANK_TRANSFER, 106000000L, "17-03-2024", null);
            generatePayment("PAY-REL567-001", "INV-SEL234-001", "SEL234", "REL567", PaymentType.INVOICING, PaymentStatus.UNPAID, PaymentMethod.BANK_TRANSFER, 455500000L, "10-12-2023", null);

//-        =============================== PAYMENT PAYANCING ===========================
//            generatePayment("PAY-DANAMON-001", "INV-AST123-001", null, "AST123", PaymentType.INVOICING, PaymentStatus.UNPAID, PaymentMethod.BANK_TRANSFER, 19650541667l, "16-08-2023", null);
//            generatePayment("PAY-IND345-001", "INV-AST123-001", "IND345", "AST123", PaymentType.INVOICING, PaymentStatus.UNPAID, PaymentMethod.BANK_TRANSFER, 201000000l, "16-08-2023", null);
//            generatePayment("PAY-DANAMON-002", "INV-AST123-001", "IND345", "AST123", PaymentType.INVOICING, PaymentStatus.UNPAID, PaymentMethod.BANK_TRANSFER, 164869022l, "16-08-2023", null);

//            generatePayment("FIN-MUL890-001", "INV-AST123-001", "IND345", "AST123", PaymentType.INVOICING, PaymentStatus.UNPAID, PaymentMethod.BANK_TRANSFER, 41217255l, "16-08-2023", null);
//            generatePayment("FIN-MUL890-002", "INV-AST123-001", "IND345", "AST123", PaymentType.INVOICING, PaymentStatus.UNPAID, PaymentMethod.BANK_TRANSFER, 41217255l, "16-08-2023", null);

//            generatePayment("PAY-IND345-001", "INV-AST123-001", "IND345", "AST123", PaymentType.INVOICING, PaymentStatus.UNPAID, PaymentMethod.BANK_TRANSFER, 201000000l, "16-08-2023", null);
//            generatePayment("PAY-IND345-001", "INV-AST123-001", "IND345", "AST123", PaymentType.INVOICING, PaymentStatus.UNPAID, PaymentMethod.BANK_TRANSFER, 201000000l, "16-08-2023", null);
//            generatePayment("PAY-IND345-001", "INV-AST123-001", "IND345", "AST123", PaymentType.INVOICING, PaymentStatus.UNPAID, PaymentMethod.BANK_TRANSFER, 201000000l, "16-08-2023", null);
//            generatePayment("PAY-IND345-001", "INV-AST123-001", "IND345", "AST123", PaymentType.INVOICING, PaymentStatus.UNPAID, PaymentMethod.BANK_TRANSFER, 201000000l, "16-08-2023", null);

        } catch (Exception e) {
            System.err.println("Seeder Payment error with : " + e);
        }
    }

    private void generatePayment(String id, String invoiceId, String senderId, String recipientId, PaymentType type, PaymentStatus status, PaymentMethod paymentMethod, Long amount, String dueDate, String paidDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date1 = null;
        Date date2 = null;
        try {
            date1 = dateFormat.parse(dueDate);
        } catch (ParseException e) {
            System.out.println("Error parsing date: " + e.getMessage());
        }

        if (paidDate != null) {
            try {
                date2 = dateFormat.parse(paidDate);
            } catch (ParseException e) {
                System.out.println("Error parsing date: " + e.getMessage());
            }
        }

        Company recipient = companyRepository.findById(recipientId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found on seeder with ID : " + recipientId));
        Company sender = companyRepository.findById(senderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found on seeder with ID : " + senderId));
        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found on seeder with ID : " + senderId));

        paymentRepository.saveAndFlush(
                Payment.builder()
                        .paymentId(id)
                        .invoice(invoice)
                        .senderId(sender)
                        .recipientId(recipient)
                        .type(type)
                        .status(status)
                        .method(paymentMethod)
                        .amount(amount)
                        .createdDate(new Date())
                        .dueDate(date1)
                        .paidDate(date2)
                        .build()
        );
    }

    private void financingSeeder() {
//        =============== FINANCING PAYABLE ================
        generateFinancingPayable("FIN-REL567-002","PAY-REL567-001", "REL567", "INV-SEL234-001",0.07d,3,455000000L,153608157D, FinancingStatus.ONGOING,"20-10-2023");
        generateFinancingPayable("FIN-UNI456-002","PAY-UNI456-001", "UNI456", "INV-REL567-001",0.07d,1,180000000L,181050000D, FinancingStatus.PENDING,"26-01-2024");
        generateFinancingPayable("FIN-DWI890-002","PAY-DWI890-001", "DWI890", "INV-GAR123-001",0.07d,2,106000000L,53464200D, FinancingStatus.REJECTED,"26-01-2024");
//        =============== FINANCING RECEIVABLE ==============
        generateFinancingReceivable("FIN-AST123-002","INV-AST123-001","AST123","23-04-2023","23-04-2023",FinancingStatus.ONGOING,FinancingType.RECEIVABLE,201000000L,4494583.33D, 196505416.67D);
        generateFinancingReceivable("FIN-GAJ456-002","INV-GAJ456-002","GAJ456","28-08-2023","28-08-2023",FinancingStatus.ONGOING,FinancingType.RECEIVABLE,210800000L,4713722.22D, 206086277.78D);
        generateFinancingReceivable("FIN-GAR123-002","INV-GAR123-001","GAR123","23-11-2023","23-11-2023",FinancingStatus.PENDING,FinancingType.RECEIVABLE,106000000L,2370277.88D, 103629722.22D);

    }

    private void generateFinancingPayable(String id, String paymentId, String recipientId, String invoiceId ,Double interest, Integer tenure, Long amount, Double installment, FinancingStatus status, String approvalDate) {
        Company recipient = companyRepository.findById(recipientId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found on seeder with id : " + recipientId));
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found on seeder with id : " + paymentId));
        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found on seeder with id : " + invoiceId));

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = null;
        try {
            date = dateFormat.parse(approvalDate);
        } catch (ParseException e) {
            System.out.println("Error parsing date: " + e.getMessage());
        }

        FinancingPayable financing = FinancingPayable.builder()
                .financingPayableId(id)
                .payment(payment)
                .company(recipient)
                .status(status)
                .amount(amount)
                .interest(interest)
                .total(amount + (amount * interest))
                .tenure(tenure)
                .monthly_installment(installment)
                .period_number(0)
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .createdDate(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .createdBy(recipient.getCompanyName())
                .build();

        financingPayableRepository.saveAndFlush(financing);

        for (int i = 1; i <= tenure; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.MONTH, i);
            Date dueDate = calendar.getTime();

            // Set the tenure status
            TenureStatus tenureStatus = (i > 1) ? TenureStatus.UPCOMING : TenureStatus.UNPAID;

            tenureRepository.saveAndFlush(
                    Tenure.builder()
                            .financingPayableId(financing)
                            .dueDate(dueDate)
                            .status(tenureStatus)
                            .Amount(installment)
                            .build()
            );
        }
    }

    private void generateFinancingReceivable(String id, String invoiceId, String recipientId, String approvalDate, String disbursmentDate, FinancingStatus status, FinancingType type, Long amount, Double fee, Double total) {
        Company recipient = companyRepository.findById(recipientId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found on seeder with ID : " + recipientId));
        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid Id invoice on seeder recevivable with ID : "+ invoiceId) );
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = null;
        Date date2 = null;
        try {
            date = dateFormat.parse(disbursmentDate);
            date2 = dateFormat.parse(approvalDate);
        } catch (ParseException e) {
            System.out.println("Error parsing date: " + e.getMessage());
        }
        financingReceivableRepository.saveAndFlush(
                FinancingReceivable.builder()
                        .financingId(id)
                        .invoice(invoice)
                        .company(recipient)
                        .modifiedDate(date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                        .createdDate(LocalDateTime.now())
                        .disbursment_date(date)
                        .status(status)
                        .financingType(type)
                        .amount(amount)
                        .fee(fee)
                        .total(total)
                        .build()
        );
    }

}
