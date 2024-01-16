package com.danamon.autochain.controller.dashboard;


import com.danamon.autochain.dto.DataResponse;
import com.danamon.autochain.dto.backoffice_dashboard.CompanySummaryResponse;
import com.danamon.autochain.dto.user_dashboard.LimitResponse;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.service.FinancingService;
import com.danamon.autochain.service.impl.FinancingServiceImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/api/dashboard")
public class BackOfficeDashboardController {
    private final FinancingService financingService;
    private final CompanyService companyService;
    @GetMapping
    @PermitAll
    public ResponseEntity<?> getStat(){
        FinancingStatResponse allFinanceStat = financingService.getAllFinanceStat();
        DataResponse<FinancingStatResponse> response = DataResponse.<FinancingStatResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Success Get Data")
                .data(allFinanceStat)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get/all")
    @PermitAll
    public ResponseEntity<?> getAll(){
        List<FinancingServiceImpl.BackofficeFinanceResponse> backofficeFinanceResponses = financingService.backoffice_get_all_financing();
        DataResponse<List<FinancingServiceImpl.BackofficeFinanceResponse>> response = DataResponse.<List<FinancingServiceImpl.BackofficeFinanceResponse>>builder()
                .data(backofficeFinanceResponses)
                .message("success get all data")
                .statusCode(HttpStatus.OK.value())
                .build();
        return ResponseEntity.ok(response);
    }
    public record FinancingStatResponse(Long financing, Long pending, Long outstanding){

    }

    @GetMapping("/company-summary")
    public ResponseEntity<?> getLimit(){
        CompanySummaryResponse companyResponse = companyService.getCompanySummary();
        DataResponse<CompanySummaryResponse> response = DataResponse.<CompanySummaryResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Success get company summary")
                .data(companyResponse)
                .build();
        return ResponseEntity.ok(response);
    }
}
