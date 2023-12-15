package com.danamon.autochain.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class RandomPasswordUtil {
    public String generateRandomPassword(int length) {
        // Your logic to generate a random password
        // Example logic using SecureRandom to generate a random password
        SecureRandom random = new SecureRandom();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            sb.append(characters.charAt(randomIndex));
        }
        return sb.toString();
    }
}
