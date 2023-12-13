package com.danamon.autochain.controller;

import com.danamon.autochain.dto.DataResponse;
import com.danamon.autochain.dto.auth.*;
import com.danamon.autochain.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping
    public String home(){
        return "Hello World!";
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterRequest request){
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
    public ResponseEntity<?> loginUser(@RequestBody UserLoginRequest request){
        String otp = authService.loginUser(request);
//        UserLoginResponse data = (UserLoginResponse) otp;
        DataResponse<String> response = DataResponse.<String>builder()
                .message("User Successfully login")
                .statusCode(HttpStatus.OK.value())
                .data(otp)
                .build();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/varifyOtp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpRequest otpRequest){
        UserLoginResponse userLoginResponse = authService.verifyOneTimePassword(otpRequest);
        DataResponse<UserLoginResponse> response = DataResponse.<UserLoginResponse>builder()
                .message("Success Verify OTP Code")
                .statusCode(HttpStatus.OK.value())
                .data(userLoginResponse)
                .build();
        return ResponseEntity.ok(response);
    }
}
