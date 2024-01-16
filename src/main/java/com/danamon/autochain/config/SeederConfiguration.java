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

import javax.management.relation.Role;
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
//            backofficeSeeder();
//            userSeeder();
//            partnershipSeeder();
//            invoiceAndPaymentSeeder();
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

        Roles admin = rolesRepository.findByRoleName("SUPER_ADMIN").orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "roles not exist"));

        List<UserRole> role2 = new ArrayList<>();
        // Create and save seed data for Credential entity
        Credential adminCredential2 = new Credential();
        adminCredential2.setEmail("backoffice2@gmail.com");
        adminCredential2.setUsername("backoffice2");
        adminCredential2.setPassword(bCryptUtil.hashPassword(bo_password));
        adminCredential2.setActor(ActorType.BACKOFFICE);
        adminCredential2.setRoles(role2);
        adminCredential2.setModifiedDate(LocalDateTime.now());
        adminCredential2.setCreatedDate(LocalDateTime.now());
        adminCredential2.setCreatedBy("BO2");
        adminCredential2.setModifiedBy("BO2");

        BackOffice backOffice2 = new BackOffice();
        backOffice2.setCredential(adminCredential2);

        role.add(
                UserRole.builder()
                        .role(admin)
                        .credential(adminCredential2)
                        .build()
        );

        credentialRepository.saveAndFlush(adminCredential2);
        backOfficeRepository.saveAndFlush(backOffice2);
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
                .method(PaymentMethod.BANK_TRANSFER)
                .status(PaymentStatus.UNPAID)
                .build();

        paymentRepository.saveAndFlush(payment2);
    }
}
