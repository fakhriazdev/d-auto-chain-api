package com.danamon.autochain.service.impl;


import com.danamon.autochain.dto.user.UserResponse;
import com.danamon.autochain.entity.BackOfficeUser;
import com.danamon.autochain.entity.User;
import com.danamon.autochain.repository.BackOfficeRepository;
import com.danamon.autochain.repository.UserRepository;
import com.danamon.autochain.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

//    private final BackOfficeService backOfficeService;
//    private final UserService userService;

    private final UserRepository userRepository;
    private final BackOfficeRepository backOfficeRepository;

//    public UserRegisterResponse registerUser(UserRegisterRequest request){
//        return userService.registerUser(request);
//    }
//
//    public BackOfficeRegisterResponse registerBackOffice(BackOfficeRegisterRequest request){
//        return backOfficeService.registerBackOffice(request);
//    }

//    public LoginResponse authenticateUser(LoginRequest request, String userType) {
//
//        if ("user".equalsIgnoreCase(userType)) {
//            return userService.loginUser(request);
//        } else if ("backoffice".equalsIgnoreCase(userType)) {
//            return  backOfficeService.loginBackOffice(request);
//        }
//        throw new IllegalArgumentException("Invalid user type");
//    }

    @Override
    public UserDetails loadToken(String id, String userType){
        if("user".equalsIgnoreCase(userType)) return loadUserByUserId(id);
        else if("backoffice".equalsIgnoreCase(userType)) return backOfficeLoadUserByUserId(id);
        throw new IllegalArgumentException("Invalid user type");
    }

    @Override
    public User loadUserByUserId(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("invalid credential"));

        log.info("End loadByUserId");
        return User.builder()
                .user_id(user.getUser_id())
                .email(user.getEmail())
                .password(user.getPassword())
                .user_type(user.getUser_type())
                .build();
    }

    @Override
    public BackOfficeUser backOfficeLoadUserByUserId(String id) {
        BackOfficeUser user = backOfficeRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("invalid credential"));

        log.info("End loadByUserId");
        return BackOfficeUser.builder()
                .user_id(user.getUser_id())
                .email(user.getEmail())
                .password(user.getPassword())
                .user_role(user.getUser_role())
                .build();
    }

    @Override
    public UserResponse getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        User info = (User) authentication.getPrincipal();
        return UserResponse.builder()
                .id(info.getUser_id())
                .username(info.getUsername())
                .role(info.getUser_type().getName())
                .build();
    }


    @Override
    public UserResponse getBackOfficeInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        BackOfficeUser info = (BackOfficeUser) authentication.getPrincipal();
        return UserResponse.builder()
                .id(info.getUser_id())
                .username(info.getUsername())
                .role(info.getUser_role().getName())
                .build();
    }
}
