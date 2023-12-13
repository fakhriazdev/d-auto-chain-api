package com.danamon.autochain.service;

import com.danamon.autochain.dto.auth.*;
import com.danamon.autochain.entity.User;

public interface AuthService {
    UserRegisterResponse registerUser(UserRegisterRequest request);
    String loginUser(UserLoginRequest request);
    UserLoginResponse verifyOneTimePassword(OtpRequest otpRequest);
    UserRegisterResponse registerBackOffice(AuthRequest request);
    UserLoginResponse loginBackOffice(AuthRequest request);
}
