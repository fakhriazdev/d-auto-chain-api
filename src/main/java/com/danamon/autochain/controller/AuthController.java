package com.danamon.autochain.controller;

import com.danamon.autochain.dto.DataResponse;
import com.danamon.autochain.dto.auth.*;
import com.danamon.autochain.service.AuthService;
import com.danamon.autochain.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterRequest request){
        UserRegisterResponse user = authService.registerUser(request);
        DataResponse<UserRegisterResponse> response = DataResponse.<UserRegisterResponse>builder()
                .message("User Successfully Register")
                .statusCode(HttpStatus.OK.value())
                .data(user)
                .build();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request){
        String data = authService.loginUser(request);
        DataResponse<String> response = DataResponse.<String>builder()
                .message("User Successfully login")
                .statusCode(HttpStatus.OK.value())
                .data(data)
                .build();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String logoutMessage = authService.logout();

        request.getSession().invalidate();

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JSESSIONID".equals(cookie.getName())) {
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                    break;
                }
            }
        }

        new SecurityContextLogoutHandler().logout(request, response, null);

        DataResponse<String> responseLogout = DataResponse.<String>builder()
                .message(logoutMessage)
                .statusCode(HttpStatus.OK.value())
                .data(null)
                .build();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseLogout);
    }

    @PostMapping("/verifyOtp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpRequest otpRequest){
        LoginResponse userLoginResponse = authService.verifyOneTimePassword(otpRequest);
        DataResponse<LoginResponse> response = DataResponse.<LoginResponse>builder()
                .message("Success Verify OTP Code")
                .statusCode(HttpStatus.OK.value())
                .data(userLoginResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request){
        String message = authService.changePassword(request);
        DataResponse<String> response = DataResponse.<String>builder()
                .data(message)
                .statusCode(HttpStatus.CREATED.value())
                .message("Success!!")
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgetPassword(@RequestParam(name = "email") String email){
        String message = authService.getByEmail(email);
        DataResponse<String> response = DataResponse.<String>builder()
                .data(message)
                .statusCode(HttpStatus.OK.value())
                .message("Success get user data")
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/recovery-password")
    public ResponseEntity<?> recoveryPassword(@RequestBody RequestRecoveryPassword request){
        authService.updatePassword(request.getId(), request.getNewPassword());
        return ResponseEntity.ok("Success fully update password");
    }
}
