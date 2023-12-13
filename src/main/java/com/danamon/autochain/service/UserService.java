package com.danamon.autochain.service;

import com.danamon.autochain.dto.auth.LoginRequest;
import com.danamon.autochain.dto.auth.LoginResponse;
import com.danamon.autochain.dto.auth.UserRegisterRequest;
import com.danamon.autochain.dto.auth.UserRegisterResponse;
import com.danamon.autochain.dto.user.UserRequest;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.Credential;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService {
    public UserRegisterResponse createNew(Credential credential, Company company);
}

