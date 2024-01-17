package com.danamon.autochain.dto.backoffice;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BackOfficeUserResponse {
    private String username;
    private String email;
    private String id;
    private String roles;
    private String name;
    private List<String> companies;
}
