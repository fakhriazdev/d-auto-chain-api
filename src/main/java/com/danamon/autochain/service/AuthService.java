package com.danamon.autochain.service;

import com.danamon.autochain.dto.user.UserResponse;
import com.danamon.autochain.entity.BackOfficeUser;
import com.danamon.autochain.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
public interface AuthService {

    UserDetails loadToken(String id, String userType);

    BackOfficeUser backOfficeLoadUserByUserId(String id);

    User loadUserByUserId(String id);

    UserResponse getUserInfo();
    UserResponse getBackOfficeInfo();

}
