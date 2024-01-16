package com.danamon.autochain.dto.auth;
import com.danamon.autochain.dto.user.UserRoleResponse;
import com.danamon.autochain.entity.Roles;
import com.danamon.autochain.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.management.relation.Role;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CredentialResponse {
    private String id;
    private String username;
    private String actor;
    private List<String> role;
}
