package com.danamon.autochain.service;

import com.danamon.autochain.dto.financing.*;
import com.danamon.autochain.entity.FinancingReceivable;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface FinancingService {
    Map<String,Double> get_limit();
    List<FinancingReceivable> receivable_financing(List<ReceivableRequest> request);
    Page<FinancingResponse> getAll(SearchFinancingRequest request);
    ReceivableDetailResponse get_detail_receivable(String financing_id);

//    ============================ OFFICE =================================
    Page<FinancingResponse> backoffice_getAll(SearchFinancingRequest request);

    AcceptResponse backoffice_accept(String financing_id);
    RejectResponse backoffice_reject(String financing_id);

}
