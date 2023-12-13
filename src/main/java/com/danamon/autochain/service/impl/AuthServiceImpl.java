package com.danamon.autochain.service.impl;


import com.danamon.autochain.dto.auth.*;
import com.danamon.autochain.service.BackOfficeService;
import com.danamon.autochain.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl {

    private final BackOfficeService backOfficeService;
    private final UserService userService;

    public UserRegisterResponse registerUser(UserRegisterRequest request){
        return userService.registerUser(request);
    }

    public BackOfficeRegisterResponse registerBackOffice(BackOfficeRegisterRequest request){
        return backOfficeService.registerBackOffice(request);
    }

    public LoginResponse authenticateUser(LoginRequest request, String userType) {

        if ("user".equalsIgnoreCase(userType)) {
            return userService.loginUser(request);
        } else if ("backoffice".equalsIgnoreCase(userType)) {
            return  backOfficeService.loginBackOffice(request);
        }
        throw new IllegalArgumentException("Invalid user type");
    }

    public UserDetails loadToken(String id, String userType){
        if("user".equalsIgnoreCase(userType)) return userService.loadUserByUserId(id);
        else if("backoffice".equalsIgnoreCase(userType)) return backOfficeService.loadUserByUserId(id);
        throw new IllegalArgumentException("Invalid user type");
    }


}
