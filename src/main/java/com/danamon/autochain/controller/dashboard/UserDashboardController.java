package com.danamon.autochain.controller.dashboard;


import com.danamon.autochain.dto.DataResponse;
import com.danamon.autochain.dto.user_dashboard.LimitResponse;
import com.danamon.autochain.service.FinancingService;
import com.danamon.autochain.service.InvoiceService;
import com.danamon.autochain.service.PaymentService;
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
@RequestMapping("/api/user-dashboard")
public class UserDashboardController {
    private final PaymentService paymentService;
    private final InvoiceService invoiceService;
    private final FinancingService financingService;
    @GetMapping("/limit")
    public ResponseEntity<?> getLimit(){
        LimitResponse limitResponse = paymentService.getLimitDashboard();
        DataResponse<LimitResponse> response = DataResponse.<LimitResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Success Get Limit")
                .data(limitResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cash/payable")
    private ResponseEntity<?> getCashCycle(){
        Long totalPaidInvoicePayable = invoiceService.getTotalPaidInvoicePayable();
        Long totalUnpaidInvoicePayable = invoiceService.getTotalUnpaidInvoicePayable();

        Long totalPaidFinancingPayable = financingService.getTotalPaidFinancingPayable();
        Long totalUnpaidFinancingPayable = financingService.getTotalUnpaidFinancingPayable();

        CycleCash cycleCash = new CycleCash(totalPaidInvoicePayable,totalUnpaidInvoicePayable, totalPaidFinancingPayable, totalUnpaidFinancingPayable);

        DataResponse<CycleCash> response = DataResponse.<CycleCash>builder()
                .data(cycleCash)
                .message("Success Get Data")
                .statusCode(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    private record CycleCash(Long paidInvoice, Long unpaidInvoice, Long paidFinancingPayable, Long unpaidFinancingPayable){}
}
