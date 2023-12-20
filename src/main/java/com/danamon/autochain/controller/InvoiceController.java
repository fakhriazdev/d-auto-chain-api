package com.danamon.autochain.controller;

import com.danamon.autochain.dto.DataResponse;
import com.danamon.autochain.dto.Invoice.request.RequestInvoice;
import com.danamon.autochain.dto.Invoice.response.ResponseInvoice;
import com.danamon.autochain.entity.Invoice;
import com.danamon.autochain.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoice")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;
    @PostMapping
    public ResponseEntity<?> invoiceGeneration(@RequestBody RequestInvoice request){
        ResponseInvoice responseInvoice = invoiceService.invoiceGeneration(request);
        DataResponse<ResponseInvoice> response = DataResponse.<ResponseInvoice>builder()
                .data(responseInvoice)
                .message("Success Generate Invoice")
                .statusCode(HttpStatus.CREATED.value())
                .build();
        return ResponseEntity.ok(response);
    }
}
