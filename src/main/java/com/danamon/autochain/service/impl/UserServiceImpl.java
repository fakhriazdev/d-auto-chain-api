package com.danamon.autochain.service.impl;

import com.danamon.autochain.constant.UserRoleType;
import com.danamon.autochain.dto.auth.LoginRequest;
import com.danamon.autochain.dto.auth.LoginResponse;
import com.danamon.autochain.dto.auth.UserRegisterRequest;
import com.danamon.autochain.dto.auth.UserRegisterResponse;
import com.danamon.autochain.entity.User;
import com.danamon.autochain.repository.UserRepository;
import com.danamon.autochain.security.BCryptUtil;
import com.danamon.autochain.security.JwtUtil;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BCryptUtil bCryptUtil;
    private final JwtUtil jwtUtil;
    private final ValidationUtil validationUtil;
    private final AuthenticationManager authenticationManager;


    @Override // REGISTER USER
    @Transactional(rollbackFor = Exception.class)
    public UserRegisterResponse registerUser(UserRegisterRequest request) {
        try {
            log.info("Start register user");
            validationUtil.validate(request);

            Optional<User> username = userRepository.findByUsername(request.getUsername());
            Optional<User> email = userRepository.findByEmail(request.getEmail());
//            Optional<Company> company = companyRepository.findById(request.getCompany_id());

//            if (company.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "company not exist, invalid with ID "+ company.get().getCompany_id());
            if (username.isPresent() || email.isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "username / email already exist");

            User dataUser = User.builder()
                    .username(request.getUsername().toLowerCase())
                    .email(request.getEmail().toLowerCase())
                    .password(bCryptUtil.hashPassword(request.getPassword()))
                    .user_type(UserRoleType.ADMIN)
//                    .company_id(company.get())
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

    @Override // LOGIN USER
    public LoginResponse loginUser(LoginRequest request) {
        validationUtil.validate(request);

        User email = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "invalid email"));

        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getEmail().toLowerCase(),
                request.getPassword()
        ));

        SecurityContextHolder.getContext().setAuthentication(authenticate);
        boolean validAuth = SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
        if(validAuth){
            User user = (User) authenticate.getPrincipal();
            System.out.println(user.toString());
            String token = jwtUtil.generateTokenUser(user);
            return LoginResponse.builder()
                    .username(user.getUsername())
                    .user_id(user.getUser_id())
                    .user_type(user.getUser_type().getName())
                    .actor("user".toUpperCase())
                    .token(token)
                    .build();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "invalid credential");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()){
            User user = userOptional.get();
            return user;
        } else {
            throw new UsernameNotFoundException("Invalid credentials");
        }
    }

}

