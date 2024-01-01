package com.danamon.autochain.controller.backOffice;

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
@RequestMapping("/api/financing/backoffice")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class FinancingBackOfficeController {

    private final FinancingService financingService;

//    ========================================== RECEIVABLE =====================================================

    @GetMapping("/receivable")
//    @PreAuthorize("hasAnyAuthority('INVOICE_STAFF','SUPER_USER','SUPER_ADMIN')")
    public ResponseEntity<?> get_all_financing_backOffice(
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

    @GetMapping("/receivable/approve/{financing_id}")
    public ResponseEntity<?> approve_financing_receivable_backoffice(@PathVariable(name = "financing_id") String financing_id){
        ReceivableDetailResponse data = financingService.get_detail_receivable(financing_id);
        DataResponse<ReceivableDetailResponse> response = DataResponse.<ReceivableDetailResponse>builder()
                .message("Success get details")
                .statusCode(200)
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/receivable/reject/{financing_id}")
    public ResponseEntity<?> reject_financing_receivable_backoffice(@PathVariable(name = "financing_id") String financing_id){
        ReceivableDetailResponse data = financingService.get_detail_receivable(financing_id);
        DataResponse<ReceivableDetailResponse> response = DataResponse.<ReceivableDetailResponse>builder()
                .message("Success get details")
                .statusCode(200)
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

//    ====================================== PAYABLE =====================================

    @GetMapping("/payable")
//    @PreAuthorize("hasAnyAuthority('INVOICE_STAFF','SUPER_USER','SUPER_ADMIN')")
    public ResponseEntity<?> get_all_financing_payable_backoffice(
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

    @GetMapping("/payable/approve/{financing_id}")
    public ResponseEntity<?> approve_financing_payable_backoffice(@PathVariable(name = "financing_id") String financing_id){
        ReceivableDetailResponse data = financingService.get_detail_receivable(financing_id);
        DataResponse<ReceivableDetailResponse> response = DataResponse.<ReceivableDetailResponse>builder()
                .message("Success get details")
                .statusCode(200)
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/payable/reject/{financing_id}")
    public ResponseEntity<?> reject_financing_payable_backoffice(@PathVariable(name = "financing_id") String financing_id){
        ReceivableDetailResponse data = financingService.get_detail_receivable(financing_id);
        DataResponse<ReceivableDetailResponse> response = DataResponse.<ReceivableDetailResponse>builder()
                .message("Success get details")
                .statusCode(200)
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }
}

