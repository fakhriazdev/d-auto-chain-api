package com.danamon.autochain.controller.backOffice;

import com.danamon.autochain.dto.DataResponse;
import com.danamon.autochain.dto.PagingResponse;
import com.danamon.autochain.dto.financing.*;
import com.danamon.autochain.service.FinancingService;
import com.danamon.autochain.util.PagingUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/backoffice/financing")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class FinancingBackOfficeController {

    private final FinancingService financingService;

    @PostMapping("/approve")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<?> approve_financing_receivable_backoffice(@RequestBody AcceptRequest request){
        AcceptResponse data = financingService.backoffice_accept(request);
        DataResponse<AcceptResponse> response = DataResponse.<AcceptResponse>builder()
                .message("Success get details")
                .statusCode(200)
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reject")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<?> reject_financing_receivable_backoffice(@RequestBody RejectRequest request){
        RejectResponse data = financingService.backoffice_reject(request);
        DataResponse<RejectResponse> response = DataResponse.<RejectResponse>builder()
                .message("Success get details")
                .statusCode(200)
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<?> get_all_financing_payable_backoffice(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "asc") String direction,
            @RequestParam(required = false) String type,
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
                .type(type)
                .build();

        Page<FinancingResponse> data = financingService.backoffice_get_all_financing(request);

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

}

