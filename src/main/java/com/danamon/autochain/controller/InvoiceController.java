package com.danamon.autochain.controller;

import com.danamon.autochain.dto.DataResponse;
import com.danamon.autochain.dto.Invoice.request.RequestInvoice;
import com.danamon.autochain.dto.Invoice.response.InvoiceDetailResponse;
import com.danamon.autochain.dto.Invoice.response.InvoiceResponse;
import com.danamon.autochain.service.InvoiceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/{id}")
    public ResponseEntity<?> invoiceDetailPayable(@PathVariable(name = "id")String id){
        InvoiceDetailResponse invoiceDetail = invoiceService.getInvoiceDetail(id);
        DataResponse<InvoiceDetailResponse> response = DataResponse.<InvoiceDetailResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Success Get Invoice Data")
                .data(invoiceDetail)
                .build();
        return ResponseEntity.ok(response);
    }
}
