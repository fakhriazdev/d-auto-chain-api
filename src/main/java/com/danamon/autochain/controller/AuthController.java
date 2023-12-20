package com.danamon.autochain.controller;

import com.danamon.autochain.dto.DataResponse;
import com.danamon.autochain.dto.auth.*;
import com.danamon.autochain.service.AuthService;
import com.danamon.autochain.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok("Logout successful");
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
