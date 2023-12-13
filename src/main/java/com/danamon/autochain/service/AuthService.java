package com.danamon.autochain.service;

import com.danamon.autochain.dto.auth.*;
import com.danamon.autochain.entity.Credential;

import java.util.Optional;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public interface AuthService {

    UserRegisterResponse registerUser(UserRegisterRequest request);
    String loginUser(UserLoginRequest request);
    UserLoginResponse verifyOneTimePassword(OtpRequest otpRequest);
    UserRegisterResponse registerBackOffice(AuthRequest request);
    UserLoginResponse loginBackOffice(AuthRequest request);
    String getByEmail(String email);
    void updatePassword(String id, String Password);
}
