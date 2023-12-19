package com.danamon.autochain.controller;

import com.danamon.autochain.dto.DataResponse;
import com.danamon.autochain.dto.auth.UserRegisterResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    @GetMapping
    @PreAuthorize("hasAnyAuthority('INVOICE_STAFF', 'SUPER_USER')")
    public ResponseEntity<?> dashboardUser(){
        DataResponse<String> response = DataResponse.<String>builder()
                .message("User Successfully enter")
                .statusCode(HttpStatus.OK.value())
                .data("anda berada di dashboard")
                .build();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
