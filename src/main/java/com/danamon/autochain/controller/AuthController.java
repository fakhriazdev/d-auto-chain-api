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

    //for admin backoffice
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

    //for relationship backoffice
    @PostMapping("/register-relationship-manager")
    public ResponseEntity<?> registerRelationshipManager(@RequestBody UserRegisterRequest request){
        request.setUserRole(UserRole.ROLE_BACKOFFICE_RELATIONSHIP_MANAGER);

        return ResponseEntity.ok(request);
    }

    //for credit analyst backoffice
    @PostMapping("/register-credit-analyst")
    public ResponseEntity<?> registerCreditAnalyst(@RequestBody UserRegisterRequest request){
        request.setUserRole(UserRole.ROLE_BACKOFFICE_CREDIT_ANALYST);
        return ResponseEntity.ok(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserLoginRequest request){
        String otp = authService.loginUser(request);
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

    @PostMapping("/forget-password")
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
