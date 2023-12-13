package com.danamon.autochain.service.impl;


import com.danamon.autochain.constant.ActorType;
import com.danamon.autochain.constant.RoleType;
import com.danamon.autochain.dto.auth.*;
import com.danamon.autochain.entity.BackOffice;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.Credential;
import com.danamon.autochain.entity.User;
import com.danamon.autochain.repository.BackOfficeRepository;
import com.danamon.autochain.repository.CompanyRepository;
import com.danamon.autochain.repository.CredentialRepository;
import com.danamon.autochain.repository.UserRepository;
import com.danamon.autochain.security.BCryptUtil;
import com.danamon.autochain.security.JwtUtil;
import com.danamon.autochain.service.AuthService;
import com.danamon.autochain.service.BackOfficeService;
import com.danamon.autochain.service.UserService;
import com.danamon.autochain.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {


    private final JwtUtil jwtUtil;
    private final BCryptUtil bCryptUtil;
    private final ValidationUtil validationUtil;
    private final AuthenticationManager authenticationManager;

    private final UserService userService;
    private final CompanyRepository companyRepository;
    private final CredentialRepository credentialRepository;
    private final BackOfficeRepository backOfficeRepository;

    @Override
    public UserRegisterResponse registerUser(UserRegisterRequest request) {
        try {
            log.info("Start register user");
            validationUtil.validate(request);

            Optional<Credential> username = credentialRepository.findByUsername(request.getUsername());
            Optional<Credential> email = credentialRepository.findByEmail(request.getEmail());
            Company company = companyRepository.findById(request.getCompany_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "company not exist, invalid with ID "+ request.getCompany_id()));

            if (username.isPresent() || email.isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "username / email already exist");

            Credential credential = Credential.builder()
                    .email(request.getEmail())
                    .username(request.getUsername())
                    .password(bCryptUtil.hashPassword(request.getPassword()))
                    .isManufacturer(true)
                    .isSupplier(false)
                    .actor(ActorType.USER)
                    .role(RoleType.ADMIN)
                    .build();

            credentialRepository.saveAndFlush(credential);

            userService.createNew(credential, company);

            log.info("End register user");

            return UserRegisterResponse.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .roleType(credential.getRole().getName())
                    .build();

        } catch (DataIntegrityViolationException e) {
            log.error("Error register user: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "user already exist");
        }
    }

    @Override
    public LoginResponse loginUser(LoginRequest request) {
        validationUtil.validate(request);

        Credential email = credentialRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "invalid email"));

        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getEmail().toLowerCase(),
                request.getPassword()
        ));

        SecurityContextHolder.getContext().setAuthentication(authenticate);
        boolean validAuth = SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
        if(validAuth){
            Credential user = (Credential) authenticate.getPrincipal();
            System.out.println(user.toString());
            String token = jwtUtil.generateTokenUser(user);
            return LoginResponse.builder()
                    .username(user.getUsername())
                    .credential_id(user.getCredential_id())
                    .userType(user.getRole().getName().toUpperCase())
                    .actorType(user.getActor().getName().toUpperCase())
                    .token(token)
                    .build();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "invalid credential");
        }
    }

    @Override
    public BackOfficeRegisterResponse registerBackOffice(BackOfficeRegisterRequest request) {
        return null;
    }

    @Override
    public LoginResponse loginBackOffice(LoginRequest request) {
        validationUtil.validate(request);

        Credential email = credentialRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "invalid email"));

        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getEmail().toLowerCase(),
                request.getPassword()
        ));

        SecurityContextHolder.getContext().setAuthentication(authenticate);
        Credential user = (Credential) authenticate.getPrincipal();
        String token = jwtUtil.generateTokenUser(user);

//        BackOffice backOffice = backOfficeRepository.findByCredential_CredentialId(user.getCredential_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "invalid data backoffice"));

        return LoginResponse.builder()
                .username(user.getUsername())
                .credential_id(user.getCredential_id())
                .actorType(user.getActor().getName())
                .userType(user.getRole().getName())
                .token(token)
                .build();
    }
}
