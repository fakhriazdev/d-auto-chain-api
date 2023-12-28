package com.danamon.autochain.controller;

import com.danamon.autochain.dto.DataResponse;
import com.danamon.autochain.dto.company.CompanyResponse;
import com.danamon.autochain.dto.financing.ReceivableRequest;
import com.danamon.autochain.service.FinancingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/financing")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class FinancingController {

    FinancingService financingService;

    @GetMapping("/limit")
    public ResponseEntity<?> financing_limit(String company_id){
        Map<String,Double> limit = financingService.get_limit(company_id);
        DataResponse<Map<String,Double>> response = DataResponse.<Map<String,Double>>builder()
                .message("Succsess Get Financing Limit")
                .statusCode(HttpStatus.OK.value())
                .data(limit)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/receivable")
    public ResponseEntity<?> request_financing_receivable(List<ReceivableRequest> list_financing){
        List<ReceivableRequest> receivableRequests = financingService.receivable_financing(list_financing);
        DataResponse<List<ReceivableRequest>> response = DataResponse.<List<ReceivableRequest>>builder()
                .message("successfully get all company")
                .statusCode(HttpStatus.OK.value())
                .data(receivableRequests)
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/payable")
    public ResponseEntity<?> request_financing_payable(List<ReceivableRequest> list_financing){
        List<ReceivableRequest> receivableRequests = financingService.receivable_financing(list_financing);
        DataResponse<List<ReceivableRequest>> response = DataResponse.<List<ReceivableRequest>>builder()
                .message("successfully get all company")
                .statusCode(HttpStatus.OK.value())
                .data(receivableRequests)
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }


}
