package com.danamon.autochain.service;

import com.danamon.autochain.dto.Invoice.request.SearchInvoiceRequest;
import com.danamon.autochain.dto.Invoice.response.InvoiceResponse;
import com.danamon.autochain.dto.financing.FinancingResponse;
import com.danamon.autochain.dto.financing.ReceivableDetailResponse;
import com.danamon.autochain.dto.financing.ReceivableRequest;
import com.danamon.autochain.dto.financing.SearchFinancingRequest;
import com.danamon.autochain.entity.FinancingReceivable;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface FinancingService {
    Map<String,Double> get_limit();
    List<FinancingReceivable> receivable_financing(List<ReceivableRequest> request);
    Page<FinancingResponse> getAll(SearchFinancingRequest request);
    ReceivableDetailResponse get_detail_receivable(String financing_id);
}
