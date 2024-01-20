package com.danamon.autochain.controller;

import com.danamon.autochain.dto.DataResponse;
import com.danamon.autochain.dto.PagingResponse;
import com.danamon.autochain.dto.financing.*;
import com.danamon.autochain.entity.FinancingReceivable;
import com.danamon.autochain.service.FinancingService;
import com.danamon.autochain.util.PagingUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/financing")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class FinancingController {

    private final FinancingService financingService;

    @GetMapping("/limit")
    public ResponseEntity<?> financing_limit() {
        Map<String, Double> limit = financingService.get_limit();
        DataResponse<Map<String, Double>> response = DataResponse.<Map<String, Double>>builder()
                .message("Succsess Get Financing Limit")
                .statusCode(HttpStatus.OK.value())
                .data(limit)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //    ================================== PAYABLE FINANCING ============================================
    @PostMapping("/payable")
    public ResponseEntity<?> request_financing_payable(@RequestBody List<PayableRequest> list_financing) {
        financingService.create_financing_payable(list_financing);
        DataResponse<List<PayableRequest>> response = DataResponse.<List<PayableRequest>>builder()
                .message("successfully create financing payable")
                .statusCode(HttpStatus.OK.value())
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/payable")
    @PreAuthorize("hasAnyAuthority('INVOICE_STAFF','SUPER_USER','SUPER_ADMIN', 'FINANCE_STAFF')")
    public ResponseEntity<?> get_All_Financing_Payable(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "asc") String direction,
            @RequestParam(required = false) String status
    ) {
        page = PagingUtil.validatePage(page);
        size = PagingUtil.validateSize(size);
        direction = PagingUtil.validateDirection(direction);

        SearchFinancingRequest request = SearchFinancingRequest.builder()
                .page(page)
                .size(size)
                .direction(direction)
                .status(status)
                .build();

        Page<FinancingResponse> data = financingService.get_all_payable(request);

        PagingResponse pagingResponse = PagingResponse.builder()
                .count(data.getTotalElements())
                .totalPages(data.getTotalPages())
                .page(page)
                .size(size)
                .build();

        DataResponse<List<FinancingResponse>> response = DataResponse.<List<FinancingResponse>>builder()
                .data(data.getContent())
                .paging(pagingResponse)
                .message("Success Generate Invoice")
                .statusCode(HttpStatus.CREATED.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/payable/{financing_id}")
    public ResponseEntity<?> get_detail_financing_payable(@PathVariable(name = "financing_id") String financing_id) {
        PayableDetailResponse data = financingService.get_detail_payable(financing_id);

        DataResponse<PayableDetailResponse> response = DataResponse.<PayableDetailResponse>builder()
                .message("Success get details")
                .statusCode(200)
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

//    ================================= RECEIVABLE FINANCING ===========================================

    @PostMapping("/receivable")
    public ResponseEntity<?> request_financing_receivable(@RequestBody List<ReceivableRequest> list_financing) {
        financingService.create_financing_receivable(list_financing);
        DataResponse<List<FinancingReceivable>> response = DataResponse.<List<FinancingReceivable>>builder()
                .message("financing successfully created")
                .statusCode(HttpStatus.OK.value())
                .build();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/receivable")
    @PreAuthorize("hasAnyAuthority('INVOICE_STAFF','SUPER_USER','SUPER_ADMIN','FINANCE_STAFF')")
    public ResponseEntity<?> get_all_financing_receivable(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "asc") String direction,
            @RequestParam(required = false) String status
    ) {
        page = PagingUtil.validatePage(page);
        size = PagingUtil.validateSize(size);
        direction = PagingUtil.validateDirection(direction);

        SearchFinancingRequest request = SearchFinancingRequest.builder()
                .page(page)
                .size(size)
                .direction(direction)
                .status(status)
                .build();

        Page<FinancingResponse> data = financingService.get_all_receivable(request);

        PagingResponse pagingResponse = PagingResponse.builder()
                .count(data.getTotalElements())
                .totalPages(data.getTotalPages())
                .page(page)
                .size(size)
                .build();

        DataResponse<List<FinancingResponse>> response = DataResponse.<List<FinancingResponse>>builder()
                .data(data.getContent())
                .paging(pagingResponse)
                .message("Success Generate Invoice")
                .statusCode(HttpStatus.CREATED.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/receivable/{financing_id}")
    public ResponseEntity<?> get_detail_financing_receivable(@PathVariable(name = "financing_id") String financing_id) {
        ReceivableDetailResponse data = financingService.get_detail_receivable(financing_id);
        DataResponse<ReceivableDetailResponse> response = DataResponse.<ReceivableDetailResponse>builder()
                .message("Success get details")
                .statusCode(200)
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }
}
