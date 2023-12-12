package com.danamon.autochain.service;

import com.danamon.autochain.dto.auth.*;
import com.danamon.autochain.dto.request.SearchCompanyRequest;
import com.danamon.autochain.dto.response.CompanyResponse;
import com.danamon.autochain.dto.user.UserResponse;
import com.danamon.autochain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface CompanyService {
    Page<CompanyResponse> getAll(SearchCompanyRequest request);

    interface AuthService {
        UserRegisterResponse registerUser(UserRegisterRequest request);
        UserLoginResponse loginUser(UserLoginRequest request);
        UserRegisterResponse registerBackOffice(AuthRequest request);
        UserLoginResponse loginBackOffice(AuthRequest request);
    }

    interface UserService extends UserDetailsService {
        User loadUserByUserId(String id);
        UserResponse getUserInfo();
    }
}
