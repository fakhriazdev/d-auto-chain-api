package com.danamon.autochain.service;

import com.danamon.autochain.dto.auth.*;
import com.danamon.autochain.entity.Credential;

import java.util.Optional;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public interface AuthService {

    UserRegisterResponse registerUser(UserRegisterRequest request);
    String loginUser(LoginRequest request);
    LoginResponse verifyOneTimePassword(OtpRequest otpRequest);
    String getByEmail(String email);
    void updatePassword(String id, String Password);
}
