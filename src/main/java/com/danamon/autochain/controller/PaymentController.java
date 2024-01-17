package com.danamon.autochain.controller;

import com.danamon.autochain.constant.payment.PaymentType;
import com.danamon.autochain.dto.DataResponse;
import com.danamon.autochain.dto.Invoice.request.RequestInvoice;
import com.danamon.autochain.dto.Invoice.request.RequestInvoiceStatus;
import com.danamon.autochain.dto.Invoice.request.SearchInvoiceRequest;
import com.danamon.autochain.dto.Invoice.response.InvoiceDetailResponse;
import com.danamon.autochain.dto.Invoice.response.InvoiceResponse;
import com.danamon.autochain.dto.PagingResponse;
import com.danamon.autochain.dto.company.CompanyResponse;
import com.danamon.autochain.dto.company.UpdateCompanyRequest;
import com.danamon.autochain.dto.payment.PaymentChangeMethodRequest;
import com.danamon.autochain.dto.payment.PaymentDetailFinancing;
import com.danamon.autochain.dto.payment.PaymentResponse;
import com.danamon.autochain.dto.payment.SearchPaymentRequest;
import com.danamon.autochain.entity.Payment;
import com.danamon.autochain.service.InvoiceService;
import com.danamon.autochain.service.PaymentService;
import com.danamon.autochain.util.PagingUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping("/ongoing")
//    @PreAuthorize("hasAnyAuthority('INVOICE_STAFF','SUPER_USER','SUPER_ADMIN')")
    public ResponseEntity<?> getOngoingPayments(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "asc") String direction,
            @RequestParam(required = false, defaultValue = "payable") String groupBy,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String recipient
    ){
        page = PagingUtil.validatePage(page);
        size = PagingUtil.validateSize(size);
        direction = PagingUtil.validateDirection(direction);

        SearchPaymentRequest request = SearchPaymentRequest.builder()
                .page(page)
                .size(size)
                .direction(direction)
                .groupBy(groupBy)
                .status(status)
                .type(type)
                .recipient(recipient)
                .build();

        Page<PaymentResponse> ongoingPayments = paymentService.getOngoingPayments(request);

        PagingResponse pagingResponse = PagingResponse.builder()
                .count(ongoingPayments.getTotalElements())
                .totalPages(ongoingPayments.getTotalPages())
                .page(page)
                .size(size)
                .build();

        DataResponse<List<PaymentResponse>> response = DataResponse.<List<PaymentResponse>>builder()
                .data(ongoingPayments.getContent())
                .paging(pagingResponse)
                .message("Success get ongoing payments")
                .statusCode(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
//    @PreAuthorize("hasAnyAuthority('INVOICE_STAFF','SUPER_USER','SUPER_ADMIN')")
    public ResponseEntity<?> getHistoryPayments(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "asc") String direction,
            @RequestParam(required = false, defaultValue = "payable") String groupBy,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String recipient
    ){
        page = PagingUtil.validatePage(page);
        size = PagingUtil.validateSize(size);
        direction = PagingUtil.validateDirection(direction);

        SearchPaymentRequest request = SearchPaymentRequest.builder()
                .page(page)
                .size(size)
                .direction(direction)
                .status(status)
                .type(type)
                .groupBy(groupBy)
                .recipient(recipient)
                .build();

        Page<PaymentResponse> ongoingPayments = paymentService.getHistoryPayments(request);

        PagingResponse pagingResponse = PagingResponse.builder()
                .count(ongoingPayments.getTotalElements())
                .totalPages(ongoingPayments.getTotalPages())
                .page(page)
                .size(size)
                .build();

        DataResponse<List<PaymentResponse>> response = DataResponse.<List<PaymentResponse>>builder()
                .data(ongoingPayments.getContent())
                .paging(pagingResponse)
                .message("Success get history payments")
                .statusCode(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("method")
    public ResponseEntity<?> updateMethod(@RequestBody PaymentChangeMethodRequest request) {
        PaymentResponse paymentResponse = paymentService.changeMethodPayment(request);
        DataResponse<PaymentResponse> response = DataResponse.<PaymentResponse>builder()
                .message("successfully change method payment")
                .statusCode(HttpStatus.OK.value())
                .data(paymentResponse)
                .build();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPaymentDetail(@PathVariable(name = "id") String transactionId){
        Payment paymentDetailType = paymentService.getPaymentDetailType(transactionId);
        if (paymentDetailType.getType().equals(PaymentType.INVOICING)){
            InvoiceDetailResponse paymentDetailInvoice = paymentService.getPaymentDetailInvoice(paymentDetailType);
            DataResponse<InvoiceDetailResponse> response = DataResponse.<InvoiceDetailResponse>builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Success get data")
                    .data(paymentDetailInvoice)
                    .build();
            return ResponseEntity.ok(response);
        }else {
            PaymentDetailFinancing paymentDetailFinancing = paymentService.getPaymentDetailFinancing(paymentDetailType);
            DataResponse<PaymentDetailFinancing> response = DataResponse.<PaymentDetailFinancing>builder()
                    .data(paymentDetailFinancing)
                    .statusCode(HttpStatus.OK.value())
                    .message("Success get data")
                    .build();
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/request-financing")
    public ResponseEntity<?> getRequestFinancingPayment(){
        List<PaymentResponse> paymentForFinancingPayableResponse = paymentService.getPaymentForFinancingPayable();
        DataResponse<List<PaymentResponse>> response = DataResponse.<List<PaymentResponse>>builder()
                .message("Success get payments")
                .statusCode(HttpStatus.OK.value())
                .data(paymentForFinancingPayableResponse)
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
