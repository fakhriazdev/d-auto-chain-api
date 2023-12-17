package com.danamon.autochain.service.impl;


import com.danamon.autochain.constant.ActorType;
import com.danamon.autochain.constant.RoleType;
import com.danamon.autochain.dto.auth.*;
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
import java.util.HashMap;
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
    @Transactional(rollbackFor = Exception.class)
    public String loginUser(LoginRequest request) {
        log.info("Start login User");

        validationUtil.validate(request);

        Credential user = credentialRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getEmail().toLowerCase(),
                request.getPassword()
        ));
        SecurityContextHolder.getContext().setAuthentication(authenticate);

        HashMap<String, String> info = new HashMap<>();

        // Generate OTP
        try {
            log.info("End login User");

            OtpResponse otpResponse = OTPGenerator.generateOtp(user.getEmail());

            String otpEmail = "<html style='width: 100%;'>" +
                    "<body style='width: 100%'>" +
                    "<div style='width: 100%;'>" +
                    "<header style='color:white; width: 100%; background: #F6833C; padding: 12px 10px; top:0;'>" +
                        "<span><h2 style='text-align: center;'>D-Auto Chain</h2></span>" +
                    "</header>" +
                        "<div style='margin: auto;'>" +
                    "<div><h5><center><u>Your OTP code is</u></center></h5></div><br>" +
                    "<div><h1><center><u>"+otpResponse.getCode()+"</u></center></h1></div><br>" +
                            "<div style='width: fit-content; height: fit-content; margin: auto;'>" +
                                "<a href='"+otpResponse.getUrl()+"' style='text-decoration:none; color:white;'>" +
                                    "<div style='padding:10px 40px; height: 40px; background: #F6833C;'>" +
                                        "<h2 style='text-align:center; margin:0'>Input OTP</h2>" +
                                    "</div>" +
                                "</a>" +
                            "</div>"+
                        "</div>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            info.put("emailBody", otpEmail);

            MailSender.mailer("OTP", info, user.getEmail());
            return "Please check your email to see OTP code";
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public LoginResponse verifyOneTimePassword(OtpRequest otpRequest) {
        try {
            Boolean verifyOtp = OTPGenerator.verifyOtp(otpRequest);
            if (!verifyOtp) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        }catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while generate OTP code");
        }

        Credential user = credentialRepository.findByEmail(otpRequest.getEmail()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

            String token = jwtUtil.generateTokenUser(user);
            return LoginResponse.builder()
                    .username(user.getUsername())
                    .credential_id(user.getCredentialId())
                    .userType(user.getRole().getName().toUpperCase())
                    .actorType(user.getActor().getName().toUpperCase())
                    .token(token)
                    .build();
    }

    @Override
    public String getByEmail(String email) {
        Credential credential = credentialRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT));
        User user = userRepository.findByCredential_credentialId(credential.getCredentialId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT));

        HashMap<String,String> info = new HashMap<>();
        String urlBuilder = "http://localhost:5432/user/forget/"+user.getUser_id();
        try{    URL url = new URI(urlBuilder).toURL();
            info.put("url", url.toString());
            MailSender.mailer("Password Recovery Link", info, email);    return "Success send link for email recovery";
        }catch (MessagingException e){    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (MalformedURLException | URISyntaxException e) {    throw new RuntimeException(e);
        }
    }

    @Override
    public void updatePassword(String id, String password) {
        Credential credential = credentialRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        credential.setPassword(bCryptUtil.hashPassword(password));
        credentialRepository.saveAndFlush(credential);
    }
}
