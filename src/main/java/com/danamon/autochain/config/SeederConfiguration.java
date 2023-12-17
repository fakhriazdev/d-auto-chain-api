package com.danamon.autochain.config;

import com.danamon.autochain.constant.ActorType;
import com.danamon.autochain.constant.RoleType;
import com.danamon.autochain.entity.BackOffice;
import com.danamon.autochain.entity.Credential;
import com.danamon.autochain.repository.BackOfficeRepository;
import com.danamon.autochain.repository.CredentialRepository;
import com.danamon.autochain.security.BCryptUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SeederConfiguration implements CommandLineRunner {
    private final CredentialRepository credentialRepository;
    private final BackOfficeRepository backOfficeRepository;
    private final BCryptUtil bCryptUtil;

    private final String email = "rizdaagisa@gmail.com";
    private final String userame= "rizda";
    private final String password = "string";

    @Override
    public void run(String... args) {
        Optional<Credential> byUsername = credentialRepository.findByEmail(email);
        if(byUsername.isEmpty()){
            backofficeSeeder();
        }
    }

    public void backofficeSeeder(){
        // Create and save seed data for Credential entity
        Credential adminCredential = new Credential();
        adminCredential.setEmail(email);
        adminCredential.setUsername(userame);
        adminCredential.setPassword(bCryptUtil.hashPassword(password));
        adminCredential.setSupplier(true);
        adminCredential.setManufacturer(true);
        adminCredential.setActor(ActorType.BACKOFFICE);
        adminCredential.setRole(RoleType.SUPER_ADMIN);

        BackOffice backOffice = new BackOffice();
        backOffice.setCredential(adminCredential);

        credentialRepository.saveAndFlush(adminCredential);
        backOfficeRepository.saveAndFlush(backOffice);
    }
}
