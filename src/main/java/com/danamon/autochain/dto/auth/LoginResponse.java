package com.danamon.autochain.dto.auth;

import com.danamon.autochain.dto.user.UserRoleResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private String credential_id;
    private String token;
    private String username;
    private String actorType;
    private List<String> roleType;
    private String company_id;
}
