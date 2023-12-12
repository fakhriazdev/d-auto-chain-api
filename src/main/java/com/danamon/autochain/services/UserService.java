package com.danamon.autochain.services;

import com.danamon.autochain.dto.user.UserResponse;
import com.danamon.autochain.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    User loadUserByUserId(String id);
    UserResponse getUserInfo();
}

