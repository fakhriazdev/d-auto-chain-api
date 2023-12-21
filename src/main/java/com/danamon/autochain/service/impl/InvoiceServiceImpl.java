package com.danamon.autochain.service.impl;


import com.danamon.autochain.constant.invoice.Status;
import com.danamon.autochain.constant.invoice.Type;
import com.danamon.autochain.dto.Invoice.request.RequestInvoice;
import com.danamon.autochain.dto.Invoice.response.ResponseInvoice;
import com.danamon.autochain.entity.*;
import com.danamon.autochain.repository.InvoiceRepository;
import com.danamon.autochain.repository.UserRepository;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.service.InvoiceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

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
                .status(Status.valueOf(requestInvoice.getStatus()))
                .amount(requestInvoice.getAmount())
                .type(Type.valueOf(requestInvoice.getType()))
                .createdDate(LocalDateTime.now())
                .createdBy(principal.getCredentialId())
                .itemList(requestInvoice.getItemList())
                .build();

        invoiceRepository.saveAndFlush(invoice);

        ObjectMapper objectMapper = new ObjectMapper();

        List<ItemList> itemLists = null;
        try {
            itemLists = objectMapper.readValue(invoice.getItemList(), new TypeReference<List<ItemList>>() {
            });
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while converting string to JSON. Please contact administrator");
        }

        return ResponseInvoice.builder()
                .companyName(invoice.getRecipientId().getCompanyName())
                .Status(String.valueOf(invoice.getStatus()))
                .invNumber(invoice.getInvoiceId())
                .dueDate(invoice.getDueDate())
                .amount(invoice.getAmount())
                .type(String.valueOf(invoice.getType()))
                .itemList(itemLists)
                .build();
    }
}
