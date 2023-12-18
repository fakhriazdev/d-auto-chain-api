package com.danamon.autochain.service.impl;


import com.danamon.autochain.constant.ActorType;
import com.danamon.autochain.dto.auth.*;
import com.danamon.autochain.entity.*;
import com.danamon.autochain.repository.*;
import com.danamon.autochain.security.BCryptUtil;
import com.danamon.autochain.security.JwtUtil;
import com.danamon.autochain.service.AuthService;
import com.danamon.autochain.service.UserService;
import com.danamon.autochain.util.MailSender;
import com.danamon.autochain.util.OTPGenerator;
import com.danamon.autochain.util.ValidationUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.mail.MessagingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CredentialRepository credentialRepository;
    private final RolesRepository rolesRepository;
    private final UserRolesRepository userRolesRepository;

    @Override
    public UserRegisterResponse registerUser(UserRegisterRequest request) {
        try {
            log.info("Start register user");
            validationUtil.validate(request);

            Optional<Credential> username = credentialRepository.findByUsername(request.getUsername());
            Optional<Credential> email = credentialRepository.findByEmail(request.getEmail());
            Company company = companyRepository.findById(request.getCompany_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "company not exist, invalid with ID "+ request.getCompany_id()));

            if (username.isPresent() || email.isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "username / email already exist");

            List<Roles> getRoles = rolesRepository.findAllById(request.getRole_id());

            if(getRoles.size() != request.getRole_id().size()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "please check role ID is not exist");

            List<UserRole> userRoles = new ArrayList<>();

            Credential credential = Credential.builder()
                    .email(request.getEmail())
                    .username(request.getUsername())
                    .password(bCryptUtil.hashPassword(request.getPassword()))
                    .actor(ActorType.USER)
                    .roles(userRoles)
                    .createdBy(request.getUsername())
                    .createdDate(LocalDateTime.now())
                    .modifiedBy(request.getUsername())
                    .modifiedDate(LocalDateTime.now())
                    .build();


            getRoles.forEach(roles -> userRoles.add(
                    UserRole.builder()
                            .role(roles)
                            .credential(credential)
                            .build()
            ));


            credentialRepository.saveAndFlush(credential);
            userService.createNew(credential, company);
            userRolesRepository.saveAllAndFlush(userRoles);

            log.info("End register user");

            List<String> roleResponses = new ArrayList<>();
            getRoles.forEach(roles -> roleResponses.add(roles.getRoleName()));

            return UserRegisterResponse.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .roleType(roleResponses)
                    .build();

        } catch (DataIntegrityViolationException e) {
            log.error("Error register user: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "user already exist");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String loginUser(LoginRequest request) {
        log.info("Start login User");

        validationUtil.validate(request);

        Credential user = credentialRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid email"));

        HashMap<String, String> info = new HashMap<>();

        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getEmail().toLowerCase(),
                request.getPassword()
        ));
        SecurityContextHolder.getContext().setAuthentication(authenticate);

        // Generate OTP
        try {
            log.info("End login User");

            String url = OTPGenerator.generateURL(request.getEmail());
            OtpResponse otpResponse = OTPGenerator.getOtp();

            info.put("Code", otpResponse.getCode()+"<br>");
            info.put("Secret", otpResponse.getSecret()+"<br>");
            info.put("counter", otpResponse.getPeriod()+"<br>");
            info.put("url", url);

            MailSender.mailer("OTP", info, user.getEmail());

            return "Please check your email to see OTP code";
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"INTERNAL SERVER ERROR");
        }
    }

    @Override
    public LoginResponse verifyOneTimePassword(OtpRequest otpRequest) {
        try {
            Boolean verifyOtp = OTPGenerator.verifyOtp(otpRequest);
            if (!verifyOtp) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID OTP");
            }
        }catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"INTERNAL SERVER ERROR");
        }

        Credential user = credentialRepository.findByEmail(otpRequest.getEmail()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String token = jwtUtil.generateTokenUser(user);

        List<String> roleResponses = new ArrayList<>();
        user.getRoles().forEach(roles -> roleResponses.add(roles.getRole().getRoleName()));

        return LoginResponse.builder()
                    .username(user.getUsername())
                    .credential_id(user.getCredentialId())
                    .roleType(roleResponses)
                    .actorType(user.getActor().getName().toUpperCase())
                    .token(token)
                    .build();
    }

    @Override
    public String changePassword(ChangePasswordRequest request){
        Credential credential = credentialRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"EMAIL NOT FOUND"));

        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getEmail().toLowerCase(),
                request.getOldPassword()
        ));
        SecurityContextHolder.getContext().setAuthentication(authenticate);

        if (!authenticate.isAuthenticated()) throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "INVALID PASSWORD");

        credential.setPassword(bCryptUtil.hashPassword(request.getNewPassword()));
        credential.setModifiedDate(LocalDateTime.now());

        credentialRepository.saveAndFlush(credential);

        return "Password Succsessfuly Changed";
    }

    @Override
    public String getByEmail(String email) {
        Credential credential = credentialRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT));
        User user = userRepository.findByCredential_credentialId(credential.getCredentialId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT));

        HashMap<String,String> info = new HashMap<>();
        String urlBuilder = "http://localhost:5432/user/forget/"+user.getUser_id();
        try{    URL url = new URI(urlBuilder).toURL();
            info.put("url", url.toString());
            MailSender.mailer("Password Recovery Link", info, email);
            return "Success send link for email recovery";
        }catch (MessagingException e){    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (MalformedURLException | URISyntaxException e) {    throw new RuntimeException(e);
        }
    }

    @Override
    public void updatePassword(String id, String password) {
        Credential credential = credentialRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        credential.setPassword(bCryptUtil.hashPassword(password));
        credential.setModifiedDate(LocalDateTime.now());
        credentialRepository.saveAndFlush(credential);
    }
}
