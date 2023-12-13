package com.danamon.autochain.dto.auth;

import com.danamon.autochain.constant.UserRole;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequest {

    @NotBlank(message = "email is required")
    @Size(min = 6, max = 126, message = "must be greater than 6 character and less than 126 character")
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 6, message = "must be greater than 6 character")
    private String password;

    @NotBlank(message = "email is required")
    @Size(min = 6, message = "must be greater than 6 character")
    private String email;

    @Size(max = 128, message = "must be less than 128 character")
    private String company_id;

    private UserRole userRole;

}
