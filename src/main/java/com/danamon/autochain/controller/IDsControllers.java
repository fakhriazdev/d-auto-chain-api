package com.danamon.autochain.controller;

import com.danamon.autochain.util.IdsGeneratorUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.PermitAll;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/generator")
@SecurityRequirement(name = "Bearer Authentication")
@PermitAll
public class IDsControllers {

    @GetMapping("/invoice")
    public ResponseEntity<?> getInvoiceId(){
        return ResponseEntity.ok(IdsGeneratorUtil.generate("INV", "Test"));
    }
}
