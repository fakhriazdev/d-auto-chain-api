package com.danamon.autochain.service;

import com.danamon.autochain.dto.auth.LoginRequest;
import com.danamon.autochain.dto.auth.LoginResponse;
import com.danamon.autochain.dto.auth.UserRegisterRequest;
import com.danamon.autochain.dto.auth.UserRegisterResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService extends UserDetailsService {
//    UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException;
    UserRegisterResponse registerUser(UserRegisterRequest request);
    LoginResponse loginUser(LoginRequest request);
}

