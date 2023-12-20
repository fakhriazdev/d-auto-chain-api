package com.danamon.autochain.service.impl;


import com.danamon.autochain.dto.Invoice.request.RequestInvoice;
import com.danamon.autochain.dto.Invoice.response.ResponseInvoice;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.Credential;
import com.danamon.autochain.entity.Invoice;
import com.danamon.autochain.entity.User;
import com.danamon.autochain.repository.InvoiceRepository;
import com.danamon.autochain.repository.UserRepository;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final CompanyService companyService;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseInvoice invoiceGeneration(RequestInvoice requestInvoice) {
        //get current user login
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        //Get company data from request
        Company recipientCompany = companyService.getById(requestInvoice.getRecipientId());

        //get userDetails data (include company) by user current login
        User currentUserLogin = userRepository.findUserByCredential(principal).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User Not Found"));

        //setup Invoice
        Invoice invoice = Invoice.builder()
                .senderId(currentUserLogin.getCompany())
                .recipientId(recipientCompany)
                .dueDate(requestInvoice.getDueDate())
                .status(requestInvoice.getStatus())
                .amount(requestInvoice.getAmount())
                .type(requestInvoice.getType())
                .createdDate(LocalDateTime.now())
                .createdBy(principal.getCredentialId())
                .itemList(requestInvoice.getItemList())
                .build();

        invoiceRepository.saveAndFlush(invoice);

        return ResponseInvoice.builder()
                .companyName(invoice.getRecipientId().getCompanyName())
                .Status(invoice.getStatus())
                .invNumber(invoice.getInvoiceId())
                .dueDate(invoice.getDueDate())
                .amount(invoice.getAmount())
                .type(invoice.getType())
                .build();
    }
}
