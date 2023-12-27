package com.danamon.autochain.service;

import com.danamon.autochain.dto.auth.*;
import com.danamon.autochain.entity.Credential;

import java.util.Optional;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public interface AuthService {

    UserRegisterResponse registerUser(UserRegisterRequest request);
    String loginUser(LoginRequest request);

    String changePassword(ChangePasswordRequest request);

    LoginResponse verifyOneTimePassword(OtpRequest otpRequest);
//    UserRegisterResponse registerBackOffice(UserRegisterRequest request);
//    LoginResponse loginBackOffice(LoginRequest request);
    String getByEmail(String email);
    void updatePassword(String id, String Password);
    String logout();
}
