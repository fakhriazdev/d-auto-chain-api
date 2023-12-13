//package com.danamon.autochain.service.impl;
//
//import com.danamon.autochain.entity.BackOfficeUser;
//import com.danamon.autochain.repository.BackOfficeRepository;
//import com.danamon.autochain.security.BCryptUtil;
//import com.danamon.autochain.security.JwtUtil;
//import com.danamon.autochain.service.BackOfficeService;
//import com.danamon.autochain.util.ValidationUtil;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//import org.springframework.web.server.ResponseStatusException;
//import com.danamon.autochain.constant.BackofficeRoleType;
//import com.danamon.autochain.dto.auth.*;
//
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.transaction.annotation.Transactional;
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class BackOfficeServiceImpl implements BackOfficeService {
//    private final BackOfficeRepository backOfficeRepository;
//    private final BCryptUtil bCryptUtil;
//    private final JwtUtil jwtUtil;
//    private final ValidationUtil validationUtil;
//    private final AuthenticationManager authenticationManager;
//
//    @Override // REGISTER BACK OFFICE
//    @Transactional(rollbackFor = Exception.class)
//    public BackOfficeRegisterResponse registerBackOffice(BackOfficeRegisterRequest request) {
//        try {
//            validationUtil.validate(request);
//
//            Optional<BackOfficeUser> username = backOfficeRepository.findByUsername(request.getUsername());
//            Optional<BackOfficeUser> email = backOfficeRepository.findByEmail(request.getEmail());
//
//            if (username.isPresent() || email.isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "username / email already exist");
//
//            BackOfficeUser dataUser = BackOfficeUser.builder()
//                    .username(request.getUsername().toLowerCase())
//                    .email(request.getEmail().toLowerCase())
//                    .password(bCryptUtil.hashPassword(request.getPassword()))
//                    .user_role(BackofficeRoleType.SUPER_ADMIN)
//                    .build();
//
//            backOfficeRepository.saveAndFlush(dataUser);
//
//            return BackOfficeRegisterResponse.builder()
//                    .username(request.getUsername())
//                    .email(request.getEmail())
//                    .user_type(dataUser.getUser_role().name())
//                    .build();
//
//        } catch (DataIntegrityViolationException e) {
//            log.error("Error register backoffice: {}", e.getMessage());
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "backoffice user already exist");
//        }
//    }
//
//    @Override // LOGIN BACK OFFICE
//    public LoginResponse loginBackOffice(LoginRequest request) {
//        validationUtil.validate(request);
//
//        Optional<BackOfficeUser> email = backOfficeRepository.findByEmail(request.getEmail());
//
//        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
//                request.getEmail().toLowerCase(),
//                request.getPassword()
//        ));
//
//        SecurityContextHolder.getContext().setAuthentication(authenticate);
//        BackOfficeUser user = (BackOfficeUser) authenticate.getPrincipal();
//        String token = jwtUtil.generateTokenBackOffice(user);
//        return LoginResponse.builder()
//                .username(user.getUsername())
//                .user_id(user.getUser_id())
//                .user_type(user.getUser_role().getName())
//                .actor("backoffice".toUpperCase())
//                .token(token)
//                .build();
//    }
//
//    @Override
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        Optional<BackOfficeUser> backOfficeUserOptional = backOfficeRepository.findByEmail(email);
//        if (backOfficeUserOptional.isPresent()) {
//            BackOfficeUser backOfficeUser = backOfficeUserOptional.get();
//            return backOfficeUser;
//        } else {
//            throw new UsernameNotFoundException("Invalid credentials");
//        }
//    }
//}
