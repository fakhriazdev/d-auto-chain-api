package com.danamon.autochain.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {
    private String email;
    private String oldPassword;
    private String newPassword;
}
