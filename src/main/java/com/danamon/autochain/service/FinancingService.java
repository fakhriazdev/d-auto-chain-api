package com.danamon.autochain.service;

import com.danamon.autochain.dto.financing.ReceivableRequest;

import java.util.List;
import java.util.Map;

public interface FinancingService {
    Map<String,Double> get_limit(String company_id);
    List<ReceivableRequest> receivable_financing(List<ReceivableRequest> request);
}
