package com.danamon.autochain.controller;

import com.danamon.autochain.constant.UserRole;
import com.danamon.autochain.dto.DataResponse;
import com.danamon.autochain.dto.auth.*;
import com.danamon.autochain.entity.User;
import com.danamon.autochain.service.AuthService;
import com.danamon.autochain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

//    ===================================== USER AUTH ===========================================

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

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgetPassword(@RequestBody String email){
        String message = authService.getByEmail(email);

        DataResponse<String> response = DataResponse.<String>builder()
                .data(message)
                .statusCode(HttpStatus.OK.value())
                .message("Success get user data")
                .build();

        return ResponseEntity.ok(message);
    }

    @PutMapping("/recovery-password/{id}")
    public ResponseEntity<?> recoveryPassword(@PathVariable String id, @RequestBody String newPassword){
        authService.updatePassword(id,newPassword);

        return ResponseEntity.ok("Success fully update password");
    }
}
