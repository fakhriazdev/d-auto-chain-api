package com.danamon.autochain.service;

import com.danamon.autochain.dto.user.UserResponse;
import com.danamon.autochain.entity.UserCredential;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface AuthService extends UserDetailsService {

//    UserCredential loadUserByUserId(String id);
    UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException;

    UserCredential loadUserByUserId(String id);

    UserResponse getUserInfo();

}
