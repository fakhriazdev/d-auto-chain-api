package com.danamon.autochain.controller;

import com.danamon.autochain.dto.DataResponse;
import com.danamon.autochain.dto.Invoice.request.SearchInvoiceRequest;
import com.danamon.autochain.dto.Invoice.response.InvoiceResponse;
import com.danamon.autochain.dto.PagingResponse;
import com.danamon.autochain.dto.company.CompanyResponse;
import com.danamon.autochain.dto.financing.FinancingResponse;
import com.danamon.autochain.dto.financing.ReceivableDetailResponse;
import com.danamon.autochain.dto.financing.ReceivableRequest;
import com.danamon.autochain.dto.financing.SearchFinancingRequest;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.Credential;
import com.danamon.autochain.entity.FinancingReceivable;
import com.danamon.autochain.entity.User;
import com.danamon.autochain.repository.UserRepository;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.service.FinancingService;
import com.danamon.autochain.util.PagingUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/financing")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class FinancingController {

    private final FinancingService financingService;

    @GetMapping("/limit")
    public ResponseEntity<?> financing_limit(){
        Map<String,Double> limit = financingService.get_limit();
        DataResponse<Map<String,Double>> response = DataResponse.<Map<String,Double>>builder()
                .message("Succsess Get Financing Limit")
                .statusCode(HttpStatus.OK.value())
                .data(limit)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/receivable")
    public ResponseEntity<?> request_financing_receivable(@RequestBody List<ReceivableRequest> list_financing){
        List<FinancingReceivable> receivableRequests = financingService.receivable_financing(list_financing);
        DataResponse<List<FinancingReceivable>> response = DataResponse.<List<FinancingReceivable>>builder()
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
        List<FinancingReceivable> receivableRequests = financingService.receivable_financing(list_financing);
        DataResponse<List<FinancingReceivable>> response = DataResponse.<List<FinancingReceivable>>builder()
                .message("successfully get all company")
                .statusCode(HttpStatus.OK.value())
                .data(receivableRequests)
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/receivable")
    @PreAuthorize("hasAnyAuthority('INVOICE_STAFF','SUPER_USER','SUPER_ADMIN')")
    public ResponseEntity<?> getAllFinancing(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "asc") String direction,
            @RequestParam(required = false) String status
    ){
        page = PagingUtil.validatePage(page);
        size = PagingUtil.validateSize(size);
        direction = PagingUtil.validateDirection(direction);

        SearchFinancingRequest request = SearchFinancingRequest.builder()
                .page(page)
                .size(size)
                .direction(direction)
                .status(status)
                .build();

        Page<FinancingResponse> data = financingService.getAll(request);

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
    public ResponseEntity<?> get_detail_financing_receivable(@PathVariable(name = "financing_id") String financing_id){
        ReceivableDetailResponse data = financingService.get_detail_receivable(financing_id);
        DataResponse<ReceivableDetailResponse> response = DataResponse.<ReceivableDetailResponse>builder()
                .message("Success get details")
                .statusCode(200)
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }
}
