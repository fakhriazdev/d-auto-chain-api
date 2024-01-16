package com.danamon.autochain.service;

import com.danamon.autochain.dto.auth.CredentialResponse;
import com.danamon.autochain.entity.Credential;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface CredentialService extends UserDetailsService {
    UserDetails loadUserByUserId(String id);
    CredentialResponse getUserInfo();
}
