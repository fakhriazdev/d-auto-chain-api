package com.danamon.autochain.service;

import com.danamon.autochain.dto.auth.BackOfficeRegisterRequest;
import com.danamon.autochain.dto.auth.BackOfficeRegisterResponse;
import com.danamon.autochain.dto.auth.LoginRequest;
import com.danamon.autochain.dto.auth.LoginResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface BackOfficeService extends AuthService{

    BackOfficeRegisterResponse registerBackOffice(BackOfficeRegisterRequest request);
    LoginResponse loginBackOffice(LoginRequest request);
}
