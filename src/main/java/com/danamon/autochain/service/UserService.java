package com.danamon.autochain.service;

import com.danamon.autochain.dto.auth.LoginRequest;
import com.danamon.autochain.dto.auth.LoginResponse;
import com.danamon.autochain.dto.auth.UserRegisterRequest;
import com.danamon.autochain.dto.auth.UserRegisterResponse;

public interface UserService extends AuthService{
    UserRegisterResponse registerUser(UserRegisterRequest request);
    LoginResponse loginUser(LoginRequest request);
}

