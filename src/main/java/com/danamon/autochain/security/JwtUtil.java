package com.danamon.autochain.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.danamon.autochain.entity.Credential;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Component
@Slf4j
public class JwtUtil {
    @Value("${app.autochain.jwt-secret}")
    private String jwtSecret;
    @Value("${app.autochain.app-name}")
    private String appName;
    @Value("604800")
    private long jwtExpirationInSecond;

    public String generateTokenUser(Credential user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret.getBytes(StandardCharsets.UTF_8));
            List<String> roles = new ArrayList<>();
            user.getRoles().forEach(userRole -> roles.add(userRole.getRole().getRoleName()));
            return JWT.create()
                    .withIssuer(appName)
                    .withSubject(user.getCredentialId())
                    .withExpiresAt(Instant.now().plusSeconds(jwtExpirationInSecond))
                    .withIssuedAt(Instant.now())
                    .withClaim("actor", user.getActor().getName())
                    .withClaim("role", roles)
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            log.error("error while creating jwt token: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public boolean verifyJwtToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret.getBytes(StandardCharsets.UTF_8));
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT.getIssuer().equals(appName);
        } catch (JWTVerificationException e) {
            log.error("invalid verification JWT: {}", e.getMessage());
            return false;
        }
    }

    public Map<String, String> getUserInfoByToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret.getBytes(StandardCharsets.UTF_8));
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(token);

            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("userId", decodedJWT.getSubject());

            List<String> authoritiesList = decodedJWT.getClaim("role").asList(String.class);
            userInfo.put("actor", decodedJWT.getClaim("actor").asString());
            userInfo.put("role", (authoritiesList != null) ? Arrays.toString(authoritiesList.toArray()) : "[]");

            return userInfo;
        } catch (JWTVerificationException e) {
            log.error("invalid verification JWT: {}", e.getMessage());
            return null;
        }
    }
}
