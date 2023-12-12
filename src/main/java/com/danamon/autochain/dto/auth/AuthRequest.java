package com.danamon.autochain.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthRequest {
    @NotBlank(message = "email is required")
    @Size(min = 6, max = 126, message = "must be greater than 6 character and less than 126 character")
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 6, message = "must be greater than 6 character")
    private String password;

    @NotBlank(message = "email is required")
    @Size(min = 6, message = "must be greater than 6 character")
    private String email;

//    @NotBlank(message = "id_company is required")
    @Size(max = 128, message = "must be less than 128 character")
    private String company_id;
}
