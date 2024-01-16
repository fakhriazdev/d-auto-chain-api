package com.danamon.autochain.dto.backoffice;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BackOfficeUserResponse {
    private String username;
    private String email;
    private String userId;
    private String roles;
    private String name;
}
