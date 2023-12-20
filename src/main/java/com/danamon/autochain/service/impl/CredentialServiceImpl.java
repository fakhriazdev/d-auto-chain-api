package com.danamon.autochain.service.impl;

import com.danamon.autochain.dto.auth.CredentialResponse;
import com.danamon.autochain.dto.user.UserRoleResponse;
import com.danamon.autochain.entity.Credential;
import com.danamon.autochain.repository.CredentialRepository;
import com.danamon.autochain.service.CredentialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CredentialServiceImpl implements CredentialService {
    private final CredentialRepository credentialRepository;

    @Override
    public UserDetails loadUserByUserId(String id) {
        log.info("Start loadByUserId");
        Credential userCredential = credentialRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("invalid credential"));
        log.info("End loadByUserId");

        return userCredential;
    }

    @Override
    public CredentialResponse getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Credential userCredential = (Credential) authentication.getPrincipal();
        List<String> roles= new ArrayList<>();
        userCredential.getRoles().forEach(userRole -> roles.add(userRole.getRole().getRoleName()));

        return CredentialResponse.builder()
                .username(userCredential.getUsername())
                .role(roles)
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Start loadUserByUsername");
        Credential userCredential = credentialRepository.findByEmail(email).
                orElseThrow(() -> new UsernameNotFoundException("invalid credential"));
        log.info("End loadUserByUsername");

        return userCredential;
    }
}
