package com.danamon.autochain.service.impl;

import com.danamon.autochain.constant.FinancingStatus;
import com.danamon.autochain.dto.financing.ReceivableRequest;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.Credential;
import com.danamon.autochain.entity.FinancingReceivable;
import com.danamon.autochain.entity.User;
import com.danamon.autochain.repository.CompanyRepository;
import com.danamon.autochain.repository.FinancingRepository;
import com.danamon.autochain.repository.InvoiceRepository;
import com.danamon.autochain.repository.UserRepository;
import com.danamon.autochain.service.FinancingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class FinancingServiceImpl implements FinancingService {

    FinancingRepository financingRepository;
    InvoiceRepository invoiceRepository;
    CompanyRepository companyRepository;
    UserRepository userRepository;

    @Override
    public Map<String, Double> get_limit(String company_id) {
        Company company = companyRepository.findById(company_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid company id"));
        Map<String, Double> data = new HashMap<>();
        data.put("financing_limit", company.getFinancingLimit());
        data.put("remaining_limit", company.getRemainingLimit());
        return data;
    }

    @Override
    public List<ReceivableRequest> receivable_financing(List<ReceivableRequest> request) {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findUserByCredential(principal).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credential invalid"));
        Company company = companyRepository.findById(user.getCompany().getCompany_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "invalid company id"));
        List<FinancingReceivable> financingReceivables = new ArrayList<>();
        request.forEach(receivableRequest -> financingReceivables.add(
                        FinancingReceivable.builder()
                                .invoice(invoiceRepository.findById(receivableRequest.getInvoice_number()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "invalid invoice id")))
                                .company(company)
                                .status(FinancingStatus.PENDING)
                                .financingType("RECEIVABLE")
                                .amount(receivableRequest.getAmount())
                                .disbursment_date(receivableRequest.getDisbursment_date())
                                .createdDate(LocalDateTime.now())
                                .createdBy(user.getName())
                                .build()
                )
        );
        financingRepository.saveAllAndFlush(financingReceivables);
        return request;
    }


}
