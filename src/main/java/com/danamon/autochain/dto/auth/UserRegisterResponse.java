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
public class UserRegisterResponse {
    private String username;
    private String email;
    private List<String> roleType;
}
