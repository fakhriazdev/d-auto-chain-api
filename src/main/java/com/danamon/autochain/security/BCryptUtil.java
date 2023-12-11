package com.danamon.autochain.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BCryptUtil {
    private final PasswordEncoder passwordEncoder;
    public String hashPassword(String plainText) {
        return passwordEncoder.encode(plainText);
    }
}
