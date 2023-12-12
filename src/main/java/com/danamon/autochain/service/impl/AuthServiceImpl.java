package com.danamon.autochain.service.impl;

import com.danamon.autochain.constant.UserRoleType;
import com.danamon.autochain.dto.auth.*;
import com.danamon.autochain.entity.User;
import com.danamon.autochain.repository.UserRepository;
import com.danamon.autochain.security.BCryptUtil;
import com.danamon.autochain.security.JwtUtil;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.utils.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements CompanyService.AuthService {
    private final UserRepository userRepository;
    private final BCryptUtil bCryptUtil;

//    private final CompanyRepository companyRepository;
//    private final UserService userService;
//    private final RoleService roleService;
//    private final AdminService adminService;
    private final JwtUtil jwtUtil;
    private final ValidationUtil validationUtil;
    private final AuthenticationManager authenticationManager;
    @Value("${app.autochain.super-admin-email}")
    String superUserEmail;
    @Value("${app.autochain.super-admin-username}")
    String superUserUsername;
    @Value("${app.autochain.super-admin-password}")
    String superUserPassword;

//    @PostConstruct
//    private void init() {
//        seederUserSuperAdmin(UserRegisterRequest.builder()
//                .username(superUserUsername)
//                .email(superUserEmail)
//                .password(superUserPassword)
//                .company_id("0")
//                .build());
//    }

    private void seederUserSuperAdmin(UserRegisterRequest request) {
        log.info("Start registerSuperAdmin");
        validationUtil.validate(request);
        Optional<User> username = userRepository.findByUsername(request.getUsername());
        Optional<User> email = userRepository.findByEmail(request.getEmail());

        if(username.isPresent() || email.isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "username / email already exist");

        User dataUser = User.builder()
                .username(request.getUsername().toLowerCase())
                .email(request.getEmail())
                .password(bCryptUtil.hashPassword(request.getPassword()))
                .user_type(UserRoleType.SUPER_ADMIN)
                .build();
        userRepository.saveAndFlush(dataUser);
        log.info("End registerSuperAdmin");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserRegisterResponse registerUser(UserRegisterRequest request) {
        try {
            log.info("Start register user");
            validationUtil.validate(request);

            Optional<User> username = userRepository.findByUsername(request.getUsername());
            Optional<User> email = userRepository.findByEmail(request.getEmail());
//            companyRepository.findById(request.getCompany_id());

            if (username.isPresent() || email.isPresent()) return null;

            User dataUser = User.builder()
                    .username(request.getUsername().toLowerCase())
                    .email(request.getEmail().toLowerCase())
                    .password(bCryptUtil.hashPassword(request.getPassword()))
                    .user_type(UserRoleType.ADMIN)
                    .build();

            userRepository.saveAndFlush(dataUser);
            log.info("End register user");

            return UserRegisterResponse.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .user_type(dataUser.getUser_type().name())
                    .build();

        } catch (DataIntegrityViolationException e) {
            log.error("Error register user: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "user already exist");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserLoginResponse loginUser(UserLoginRequest request) {
        log.info("Start login User");

        validationUtil.validate(request);

        Optional<User> email = userRepository.findByEmail(request.getEmail());

        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getEmail().toLowerCase(),
                request.getPassword()
        ));

        SecurityContextHolder.getContext().setAuthentication(authenticate);
        User user = (User) authenticate.getPrincipal();
        System.out.println(user.toString());
        String token = jwtUtil.generateToken(user);
        log.info("End login User");

        return UserLoginResponse.builder()
                .username(user.getUsername())
                .user_id(user.getUser_id())
                .user_type(user.getUser_type().name())
                .token(token)
                .build();
    }


    @Override
    public UserRegisterResponse registerBackOffice(AuthRequest request) {
        return null;
    }

    @Override
    public UserLoginResponse loginBackOffice(AuthRequest request) {
        return null;
    }


}
