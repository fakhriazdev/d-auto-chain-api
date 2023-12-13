package com.danamon.autochain.service;

import com.danamon.autochain.dto.auth.*;

public interface AuthService {

    UserRegisterResponse registerUser(UserRegisterRequest request);
    LoginResponse loginUser(LoginRequest request);

    BackOfficeRegisterResponse registerBackOffice(BackOfficeRegisterRequest request);
    LoginResponse loginBackOffice(LoginRequest request);
//    UserDetails loadToken(String id, String userType);
//
//    BackOffice backOfficeLoadUserByUserId(String id);
//
//    User loadUserByUserId(String id);
//
//    UserResponse getUserInfo();
//    UserResponse getBackOfficeInfo();

}
