package com.danamon.autochain.dto.auth;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestRecoveryPassword {
    private String id;
    private String newPassword;
}
