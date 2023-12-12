package com.danamon.autochain.controller;

import com.danamon.autochain.dto.DataResponse;
import com.danamon.autochain.dto.auth.*;
import com.danamon.autochain.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CompanyService.AuthService authService;

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
        UserLoginResponse data = authService.loginUser(request);
        DataResponse<UserLoginResponse> response = DataResponse.<UserLoginResponse>builder()
                .message("User Successfully login")
                .statusCode(HttpStatus.OK.value())
                .data(data)
                .build();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
