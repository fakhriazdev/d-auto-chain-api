package com.danamon.autochain.service;

import com.danamon.autochain.dto.Invoice.request.RequestInvoice;
import com.danamon.autochain.dto.Invoice.request.RequestInvoiceStatus;
import com.danamon.autochain.dto.Invoice.request.SearchInvoiceRequest;
import com.danamon.autochain.dto.Invoice.response.InvoiceDetailResponse;
import com.danamon.autochain.dto.Invoice.response.InvoiceResponse;
import com.danamon.autochain.dto.payment.CreatePaymentRequest;
import com.danamon.autochain.dto.payment.PaymentChangeMethodRequest;
import com.danamon.autochain.dto.payment.PaymentResponse;
import com.danamon.autochain.dto.payment.SearchPaymentRequest;
import com.danamon.autochain.dto.user_dashboard.LimitResponse;
import com.danamon.autochain.entity.Payment;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PaymentService {

    void createPayment(CreatePaymentRequest request);
    void deletePayment(Payment payment);
    Page<PaymentResponse> getOngoingPayments(SearchPaymentRequest request);
//    List<PaymentResponse> getOngoingPayments(SearchPaymentRequest request);
    Page<PaymentResponse> getHistoryPayments(SearchPaymentRequest request);
//    List<PaymentResponse> getHistoryPayments(SearchPaymentRequest request);
    PaymentResponse changeMethodPayment(PaymentChangeMethodRequest request);
    LimitResponse getLimitDashboard();
}
