package com.danamon.autochain.service.impl;


import com.danamon.autochain.constant.invoice.ProcessingStatusType;
import com.danamon.autochain.constant.invoice.ReasonType;
import com.danamon.autochain.constant.invoice.Status;
import com.danamon.autochain.dto.Invoice.ItemList;
import com.danamon.autochain.dto.Invoice.request.RequestInvoice;
import com.danamon.autochain.dto.Invoice.request.RequestInvoiceStatus;
import com.danamon.autochain.dto.Invoice.request.SearchInvoiceRequest;
import com.danamon.autochain.dto.company.CompanyResponse;
import com.danamon.autochain.dto.Invoice.response.InvoiceDetailResponse;
import com.danamon.autochain.dto.Invoice.response.InvoiceResponse;
import com.danamon.autochain.entity.*;
import com.danamon.autochain.repository.InvoiceIssueLogRepository;
import com.danamon.autochain.repository.InvoiceRepository;
import com.danamon.autochain.repository.UserRepository;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.service.InvoiceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final CompanyService companyService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final InvoiceIssueLogRepository invoiceIssueLogRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvoiceResponse invoiceGeneration(RequestInvoice requestInvoice) {
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
                .status(Status.PENDING)
                .processingStatus(ProcessingStatusType.WAITING_STATUS)
                .amount(requestInvoice.getAmount())
                .createdDate(LocalDateTime.now())
                .createdBy(principal.getCredentialId())
                .itemList(requestInvoice.getItemList())
                .build();

        invoiceRepository.saveAndFlush(invoice);

        List<ItemList> itemLists = mapStringToJson(invoice.getItemList());
        return InvoiceResponse.builder()
                .companyName(invoice.getRecipientId().getCompanyName())
                .status(String.valueOf(invoice.getStatus()))
                .invNumber(invoice.getInvoiceId())
                .dueDate(invoice.getDueDate())
                .amount(invoice.getAmount())
                .itemList(itemLists)
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public InvoiceDetailResponse getInvoiceDetail(String id) {
        //get current user login
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findUserByCredential(principal).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "User Not Found"));

        // get invoice by recipient and id
        Optional<Invoice> invoiceByRecipientIdAndInvoiceId = invoiceRepository.findInvoiceByRecipientIdAndInvoiceId(user.getCompany(), id);

        if (invoiceByRecipientIdAndInvoiceId.isEmpty()){
            Invoice invoice = invoiceRepository.findInvoiceBySenderIdAndInvoiceId(user.getCompany(), id).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Invoice Not Found"));

            InvoiceDetailResponse invoiceDetailResponse = mapToInvoiceDetailResponse(invoice);
            invoiceDetailResponse.setType("Receivable");
            return invoiceDetailResponse;
        }

        // map json to string
        InvoiceDetailResponse invoiceDetailResponse = mapToInvoiceDetailResponse(invoiceByRecipientIdAndInvoiceId.get());
        invoiceDetailResponse.setType("Payable");

        return invoiceDetailResponse;
    }

    @Transactional(rollbackFor = Exception.class)
    @Deprecated
    @Override
    public void updateInvoiceIssueLog(RequestInvoiceStatus requestInvoiceStatus) {
        invoiceRepository.findById(requestInvoiceStatus.getInvNumber()).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Data Not Found"));

        updateInvoiceStatus(requestInvoiceStatus);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public InvoiceDetailResponse updateInvoiceStatus(RequestInvoiceStatus requestInvoiceStatus) {

        Invoice invoice = invoiceRepository.findById(requestInvoiceStatus.getInvNumber()).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Data Not Found"));

        if (invoice.getProcessingStatus() != null) {
            if (invoice.getProcessingStatus().equals(ProcessingStatusType.CANCEL_INVOICE)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice With Status CANCEL Cannot Be Updated");
            }
        }
        ProcessingStatusType processingStatusType;
        try {
            processingStatusType = ProcessingStatusType.valueOf(requestInvoiceStatus.getProcessingType());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown Status Type");
        }

        if (processingStatusType.equals(ProcessingStatusType.CANCEL_INVOICE)) {
            // cancel by seller
            invoice.setProcessingStatus(ProcessingStatusType.CANCEL_INVOICE);
            invoice.setStatus(Status.CANCELLED);
        } else if (processingStatusType.equals(ProcessingStatusType.REJECT_INVOICE)) {
            // rejected by buyer
            // create invoice issue log
            InvoiceIssueLog invoiceIssueLog = InvoiceIssueLog.builder()
                    .issueType(ReasonType.valueOf(requestInvoiceStatus.getReasonType()))
                    .invoice(invoice)
                    .reason(requestInvoiceStatus.getReason())
                    .build();
            invoiceIssueLogRepository.save(invoiceIssueLog);
            // update invoice
            invoice.setProcessingStatus(ProcessingStatusType.REJECT_INVOICE);
            invoice.setStatus(Status.DISPUTED);
        } else if (processingStatusType.equals(ProcessingStatusType.APPROVE_INVOICE)) {
            // approve
            invoice.setProcessingStatus(ProcessingStatusType.APPROVE_INVOICE);
            invoice.setStatus(Status.UNPAID);
        }

        invoice.setProcessingStatus(processingStatusType);
        invoiceRepository.saveAndFlush(invoice);

        return mapToInvoiceDetailResponse(invoice);
    }

    private InvoiceDetailResponse mapToInvoiceDetailResponse(Invoice invoice) {
        // map json to string
        List<ItemList> itemLists = mapStringToJson(invoice.getItemList());

        CompanyResponse companySender = companyService.findById(invoice.getSenderId().getCompany_id());
        CompanyResponse companyRecipient = companyService.findById(invoice.getRecipientId().getCompany_id());

        return InvoiceDetailResponse.builder()
                .companyFrom(companySender)
                .companyRecipient(companyRecipient)
                .invoiceId(invoice.getInvoiceId())
                .date(Date.valueOf(invoice.getCreatedDate().toLocalDate()))
                .dueDate(invoice.getDueDate())
                .processingStatus(invoice.getProcessingStatus().name())
                .itemList(itemLists)
                .build();
    }

    private List<ItemList> mapStringToJson(String itemList) {
        try {
            return objectMapper.readValue(itemList, new TypeReference<List<ItemList>>() {
            });
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while converting string to JSON. Please contact administrator");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getAll(SearchInvoiceRequest request) {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findUserByCredential(principal).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credential invalid"));
        Company recipientCompany = companyService.getById(user.getCompany().getCompany_id());

        Specification<Invoice> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getStatus() != null) {
                Predicate status = criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("status")),
                        request.getStatus().toLowerCase()
                );
                predicates.add(status);
            }

            String column = "senderId";
            assert request.getType() != null;

            if(request.getType().equals("payable")){
                column = "recipientId";
            }

            Predicate id = criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get(column)),
                    recipientCompany.getCompany_id().toLowerCase()
            );
            predicates.add(id);

            return query
                    .where(predicates.toArray(new Predicate[]{}))
                    .getRestriction();
        };

        Sort.Direction direction = Sort.Direction.fromString(request.getDirection());
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), direction , "status");
        Page<Invoice> invoices = invoiceRepository.findAll(specification, pageable);

        if(request.getType().equals("payable")){
            return invoices.map(this::mapToResponsePayable);
        } else {
            return invoices.map(this::mapToResponseReceivable);
        }
    }

    private InvoiceResponse mapToResponsePayable(Invoice invoice) {
        return InvoiceResponse.builder()
                .company_id(invoice.getSenderId().getCompany_id())
                .invNumber(invoice.getInvoiceId())
                .amount(invoice.getAmount())
                .companyName(invoice.getRecipientId().getCompanyName())
                .status(String.valueOf(invoice.getStatus()))
                .dueDate(invoice.getDueDate())
                .build();
    }

    private InvoiceResponse mapToResponseReceivable(Invoice invoice) {
        return InvoiceResponse.builder()
                .company_id(invoice.getRecipientId().getCompany_id())
                .invNumber(invoice.getInvoiceId())
                .amount(invoice.getAmount())
                .companyName(invoice.getSenderId().getCompanyName())
                .status(String.valueOf(invoice.getStatus()))
                .dueDate(invoice.getDueDate())
                .build();
    }
}
