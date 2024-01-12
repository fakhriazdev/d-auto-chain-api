package com.danamon.autochain.controller;

import com.danamon.autochain.constant.invoice.ProcessingStatusType;
import com.danamon.autochain.dto.DataResponse;
import com.danamon.autochain.dto.Invoice.request.RequestInvoice;
import com.danamon.autochain.dto.Invoice.request.RequestInvoiceStatus;
import com.danamon.autochain.dto.Invoice.request.SearchInvoiceRequest;
import com.danamon.autochain.dto.Invoice.response.InvoiceDetailResponse;
import com.danamon.autochain.dto.Invoice.response.InvoiceResponse;
import com.danamon.autochain.dto.PagingResponse;
import com.danamon.autochain.service.InvoiceService;
import com.danamon.autochain.util.PagingUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/invoice")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class InvoiceController {
    private final InvoiceService invoiceService;
    @PostMapping
    public ResponseEntity<?> invoiceGeneration(@RequestBody RequestInvoice request){
        InvoiceResponse invoiceResponse = invoiceService.invoiceGeneration(request);
        DataResponse<InvoiceResponse> response = DataResponse.<InvoiceResponse>builder()
                .data(invoiceResponse)
                .message("Success Generate Invoice")
                .statusCode(HttpStatus.CREATED.value())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('INVOICE_STAFF','SUPER_USER')")
    public ResponseEntity<?> getAllInvoice(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "asc") String direction,
            @RequestParam(required = false, defaultValue = "payable") String type,
            @RequestParam(required = false) String status
    ){
        page = PagingUtil.validatePage(page);
        size = PagingUtil.validateSize(size);
        direction = PagingUtil.validateDirection(direction);

        SearchInvoiceRequest request = SearchInvoiceRequest.builder()
                .page(page)
                .size(size)
                .direction(direction)
                .status(status)
                .type(type)
                .build();

        Page<InvoiceResponse> data = invoiceService.getAll(request);

        PagingResponse pagingResponse = PagingResponse.builder()
                .count(data.getTotalElements())
                .totalPages(data.getTotalPages())
                .page(page)
                .size(size)
                .build();

        DataResponse<List<InvoiceResponse>> response = DataResponse.<List<InvoiceResponse>>builder()
                .data(data.getContent())
                .paging(pagingResponse)
                .message("Success Generate Invoice")
                .statusCode(HttpStatus.CREATED.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/approve/{invoice_id}")
    public ResponseEntity<?> invoice_approve(@PathVariable(name = "invoice_id")String invoice_id){
        invoiceService.approve_invoice(invoice_id);
        DataResponse<?> response = DataResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Success Approve Invoice")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reject")
    public ResponseEntity<?> updateInvoiceProcessingStatus(@RequestBody RequestInvoiceStatus requestInvoiceStatus){
            InvoiceDetailResponse invoiceDetailResponse = invoiceService.updateInvoiceStatus(requestInvoiceStatus);

            DataResponse<InvoiceDetailResponse> response = DataResponse.<InvoiceDetailResponse>builder()
                    .data(invoiceDetailResponse)
                    .message("Success Updating Invoice")
                    .statusCode(HttpStatus.OK.value())
                    .build();
            return ResponseEntity.ok(response);
    }

    @GetMapping("/{invoice_id}")
    public ResponseEntity<?> invoiceDetailPayable(@PathVariable(name = "invoice_id")String invoice_id){
        InvoiceDetailResponse invoiceDetail = invoiceService.getInvoiceDetail(invoice_id);
        DataResponse<InvoiceDetailResponse> response = DataResponse.<InvoiceDetailResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Success Get Invoice Data")
                .data(invoiceDetail)
                .build();
        return ResponseEntity.ok(response);
    }
}
