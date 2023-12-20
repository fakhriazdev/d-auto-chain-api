package com.danamon.autochain.service.impl;


import com.danamon.autochain.dto.Invoice.RequestInvoice;
import com.danamon.autochain.dto.auth.CredentialResponse;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.Credential;
import com.danamon.autochain.entity.Invoice;
import com.danamon.autochain.entity.User;
import com.danamon.autochain.repository.InvoiceRepository;
import com.danamon.autochain.repository.UserRepository;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.service.CredentialService;
import com.danamon.autochain.service.InvoiceService;
import com.danamon.autochain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final CredentialService credentialService;
    private final UserRepository userRepository;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Invoice invoiceGeneration(RequestInvoice requestInvoice) {
        //get current user login
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        //validation user id in request invoice
        Credential userDetails = (Credential) credentialService.loadUserByUserId(requestInvoice.getRecipientId());

        //get userDetails data (include company) by user current login
        User currentUserLogin = userRepository.findUserByCredential(principal).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User Not Found"));

        //get userDetails data (include company) by recipient
        User recipientData = userRepository.findUserByCredential(userDetails.getUser().getCredential()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User Not Found"));

        //setup Invoice
        Invoice invoice = Invoice.builder()
                .senderId(currentUserLogin.getCompany())
                .recipientId(recipientData.getCompany())
                .dueDate(requestInvoice.getDueDate())
                .status(requestInvoice.getStatus())
                .amount(requestInvoice.getAmount())
                .type(requestInvoice.getType())
                .build();

        invoiceRepository.saveAndFlush(invoice);
        return null;
    }
}
