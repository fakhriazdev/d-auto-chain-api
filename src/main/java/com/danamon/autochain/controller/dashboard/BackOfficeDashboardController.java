package com.danamon.autochain.controller.dashboard;


import com.danamon.autochain.dto.DataResponse;
import com.danamon.autochain.service.FinancingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/api/dashboard")
public class BackOfficeDashboardController {
    private final FinancingService financingService;
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

    public record FinancingStatResponse(Long financing, Long pending, Long outstanding){

    }
}
