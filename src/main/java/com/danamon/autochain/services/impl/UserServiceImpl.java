package com.danamon.autochain.services.impl;

import com.danamon.autochain.dto.response.UserResponse;
import com.danamon.autochain.entity.User;
import com.danamon.autochain.repository.UserRepository;
import com.danamon.autochain.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userCredentialRepository;

    @Override
    public User loadUserByUserId(String id) {
        log.info("Start loadByUserId");
        User userCredential = userCredentialRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("invalid credential"));
        log.info("End loadByUserId");
        return userCredential;
    }

    @Override
    public UserResponse getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        User userCredential = (User) authentication.getPrincipal();
        return UserResponse.builder()
                .username(userCredential.getUsername())
                .role(userCredential.getUser_type().name())
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Start loadUserByUsername");
        User userCredential = userCredentialRepository.findByUsername(username).
                orElseThrow(() -> new UsernameNotFoundException("invalid credential"));
        log.info("End loadUserByUsername");
        return User.builder()
                .user_id(userCredential.getUser_id())
                .username(userCredential.getUsername())
                .password(userCredential.getPassword())
                .user_type(userCredential.getUser_type())
                .build();
    }
}

