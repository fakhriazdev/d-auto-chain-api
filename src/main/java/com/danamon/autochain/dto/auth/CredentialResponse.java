package com.danamon.autochain.dto.auth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CredentialResponse {
    private String id;
    private String username;
    private String actor;
    private String role;
}
