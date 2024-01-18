package com.danamon.autochain.service;

import com.danamon.autochain.dto.Invoice.response.InvoiceDetailResponse;
import com.danamon.autochain.dto.payment.*;
import com.danamon.autochain.dto.user_dashboard.LimitResponse;
import com.danamon.autochain.entity.Payment;
import com.danamon.autochain.service.impl.PaymentServiceImpl;
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
    PaymentDetailFinancing getPaymentDetailFinancing(Payment payment);
    InvoiceDetailResponse getPaymentDetailInvoice(Payment payment);
    Payment getPaymentDetailType(String transactionId);
    List<PaymentResponse> getPaymentForFinancingPayable();
    List<PaymentResponse> getPaymentForFinancingReceivable();
    PaymentServiceImpl.UpdatePaymentResponse updatePaymentInvoicing(String id);
    PaymentServiceImpl.UpdatePaymentResponse proceedPayment(String id);
    PaymentServiceImpl.UpdatePaymentResponse proceedPaymentInvoicing(Payment payment);
    PaymentServiceImpl.UpdatePaymentResponse proceedPaymentTenure(Payment payment);
}
