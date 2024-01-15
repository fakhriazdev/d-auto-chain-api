package com.danamon.autochain.service;

import com.danamon.autochain.controller.dashboard.BackOfficeDashboardController;
import com.danamon.autochain.dto.financing.*;
import com.danamon.autochain.service.impl.FinancingServiceImpl;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface FinancingService {
    Map<String,Double> get_limit();

//    =========================== PAYABLE ======================================
    void create_financing_payable(List<PayableRequest> request);
    Page<FinancingResponse> get_all_payable(SearchFinancingRequest request);
    PayableDetailResponse get_detail_payable(String financing_id);

//    =========================== RECEIVABLE ===================================
    void create_financing_receivable(List<ReceivableRequest> request);
    Page<FinancingResponse> get_all_receivable(SearchFinancingRequest request);
    ReceivableDetailResponse get_detail_receivable(String financing_id);

//    ============================ BACK OFFICE =================================
    Page<FinancingResponse> backoffice_get_all_financing(SearchFinancingRequest request);
    AcceptResponse backoffice_accept(AcceptRequest request);
    RejectResponse backoffice_reject(RejectRequest request);
    // ============================== DASHBOARD ====================================
    BackOfficeDashboardController.FinancingStatResponse getAllFinanceStat();
    List<FinancingServiceImpl.BackofficeFinanceResponse> backoffice_get_all_financing();

    Long getTotalPaidFinancingPayable();
    Long getTotalUnpaidFinancingPayable();
}
