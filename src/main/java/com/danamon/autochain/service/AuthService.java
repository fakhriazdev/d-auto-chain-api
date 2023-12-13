package com.danamon.autochain.service;

import com.danamon.autochain.dto.auth.*;
import com.danamon.autochain.entity.Credential;

import java.util.Optional;

public interface AuthService {

    UserRegisterResponse registerUser(UserRegisterRequest request);
    LoginResponse loginUser(LoginRequest request);

    BackOfficeRegisterResponse registerBackOffice(BackOfficeRegisterRequest request);
    LoginResponse loginBackOffice(LoginRequest request);

}
