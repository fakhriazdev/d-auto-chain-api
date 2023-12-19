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
import org.springframework.security.core.AuthenticationException;
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

        Credential user = credentialRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Bad Credential"));

        try{
            Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.getEmail().toLowerCase(),
                    request.getPassword()
            ));
            SecurityContextHolder.getContext().setAuthentication(authenticate);
            SecurityContextHolder.getContext().setAuthentication(authenticate);
        }catch (AuthenticationException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bad Credential");
        }
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
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"INTERNAL SERVER ERROR, Cannot Generate OTP Code, Please Contact Administrator");
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
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID OTP");
        }

        Credential user = credentialRepository.findByEmail(otpRequest.getEmail()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email Not Exist"));

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
        Credential credential = credentialRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Email Not Found"));
        /*User user = userRepository.findByCredential_credentialId(credential.getCredentialId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "User id Not Exist"));*/

        HashMap<String,String> info = new HashMap<>();
        String urlBuilder = "http://localhost:5432/user/forget/"+credential.getCredentialId();
        try{
            URL url = new URI(urlBuilder).toURL();
            info.put("url", url.toString());
            MailSender.mailer("Password Recovery Link", info, email);
            return "Success send link for email recovery";
        }catch (MessagingException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Send Email, Please Check Your Connection");
        } catch (MalformedURLException | URISyntaxException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot Generate URL, Please Contact Administrator");
        }
    }

    @Override
    public void updatePassword(String id, String password) {
        Credential credential = credentialRepository.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.CONFLICT, "ID Not Found"));
        credential.setPassword(bCryptUtil.hashPassword(password));
        credential.setModifiedDate(LocalDateTime.now());
        credentialRepository.saveAndFlush(credential);
    }
}
