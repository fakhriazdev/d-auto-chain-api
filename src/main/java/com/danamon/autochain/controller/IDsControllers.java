package com.danamon.autochain.controller;

import com.danamon.autochain.entity.Credential;
import com.danamon.autochain.util.IdsGeneratorUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.PermitAll;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/generator")
@SecurityRequirement(name = "Bearer Authentication")
@PermitAll
public class IDsControllers {

    @GetMapping("/invoice")
    public ResponseEntity<?> getInvoiceId() {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(IdsGeneratorUtil.generate("INV", principal.getUser().getCompany().getCompany_id()));
    }
    @GetMapping("/payment")
    public ResponseEntity<?> getPaymentId(){
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(IdsGeneratorUtil.generate("PAY", principal.getUser().getCompany().getCompany_id()));
    }
    @GetMapping("/finance")
    public ResponseEntity<?> getFinanceId(){
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(IdsGeneratorUtil.generate("FIN", principal.getUser().getCompany().getCompany_id()));
    }
}
