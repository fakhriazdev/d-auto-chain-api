package com.danamon.autochain.services;

import com.danamon.autochain.dto.auth.*;

public interface AuthService {
    UserRegisterResponse registerUser(UserRegisterRequest request);
    UserLoginResponse loginUser(UserLoginRequest request);
    UserRegisterResponse registerBackOffice(AuthRequest request);
    UserLoginResponse loginBackOffice(AuthRequest request);
}
