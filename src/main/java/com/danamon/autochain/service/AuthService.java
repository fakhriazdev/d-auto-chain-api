package com.danamon.autochain.service;

import com.danamon.autochain.dto.auth.*;

public interface AuthService {
    UserRegisterResponse registerUser(UserRegisterRequest request);
    UserLoginResponse loginUser(UserLoginRequest request);
    UserRegisterResponse registerBackOffice(AuthRequest request);
    UserLoginResponse loginBackOffice(AuthRequest request);
}
