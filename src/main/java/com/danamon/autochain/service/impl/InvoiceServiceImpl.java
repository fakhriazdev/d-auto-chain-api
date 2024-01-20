package com.danamon.autochain.service.impl;


import com.danamon.autochain.constant.RoleType;
import com.danamon.autochain.constant.invoice.ProcessingStatusType;
import com.danamon.autochain.constant.invoice.ReasonType;
import com.danamon.autochain.constant.invoice.InvoiceStatus;
import com.danamon.autochain.constant.payment.PaymentMethod;
import com.danamon.autochain.constant.payment.PaymentStatus;
import com.danamon.autochain.constant.payment.PaymentType;
import com.danamon.autochain.dto.Invoice.ItemList;
import com.danamon.autochain.dto.Invoice.request.RequestInvoice;
import com.danamon.autochain.dto.Invoice.request.RequestInvoiceStatus;
import com.danamon.autochain.dto.Invoice.request.RequestUpdateInvoice;
import com.danamon.autochain.dto.Invoice.request.SearchInvoiceRequest;
import com.danamon.autochain.dto.company.CompanyResponse;
import com.danamon.autochain.dto.Invoice.response.InvoiceDetailResponse;
import com.danamon.autochain.dto.Invoice.response.InvoiceResponse;
import com.danamon.autochain.dto.payment.CreatePaymentRequest;
import com.danamon.autochain.entity.*;
import com.danamon.autochain.repository.InvoiceIssueLogRepository;
import com.danamon.autochain.repository.InvoiceRepository;
import com.danamon.autochain.repository.PaymentRepository;
import com.danamon.autochain.repository.UserRepository;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.service.InvoiceService;
import com.danamon.autochain.service.PaymentService;
import com.danamon.autochain.util.IdsGeneratorUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final CompanyService companyService;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;
    private final InvoiceIssueLogRepository invoiceIssueLogRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve_invoice(String id) {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Invoice invoice = invoiceRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid Invoice ID : " + id));

        System.out.println(invoice.getRecipientId().getCompany_id());
        System.out.println(principal.getUser().getCompany().getCompany_id());

        if (!invoice.getRecipientId().getCompany_id().equals(principal.getUser().getCompany().getCompany_id()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You only can approve your own invoice recipient");

        if (invoice.getProcessingStatus().equals(ProcessingStatusType.REJECT_INVOICE) || invoice.getProcessingStatus().equals(ProcessingStatusType.APPROVE_INVOICE))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot approve with type: " + invoice.getProcessingStatus());

        invoice.setStatus(InvoiceStatus.UNPAID);
        invoice.setProcessingStatus(ProcessingStatusType.APPROVE_INVOICE);
        invoiceRepository.saveAndFlush(invoice);

        paymentRepository.saveAndFlush(Payment.builder()
                .paymentId(IdsGeneratorUtil.generate("PAY", invoice.getSenderId().getCompany_id()))
                .invoice(invoice)
                .recipientId(invoice.getRecipientId())
                .senderId(invoice.getSenderId())
                .method(PaymentMethod.BANK_TRANSFER)
                .type(PaymentType.INVOICING)
                .status(PaymentStatus.UNPAID)
                .amount(invoice.getAmount())
                .createdDate(new java.util.Date())
                .dueDate(invoice.getDueDate())
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvoiceResponse invoiceGeneration(RequestInvoice requestInvoice) {
        //get current user login
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        //Get company data from request
        Company recipientCompany = companyService.getById(requestInvoice.getRecipientId());

        //setup Invoice
        Invoice invoice = Invoice.builder()
                .invoiceId(IdsGeneratorUtil.generate("INV", principal.getUser().getCompany().getCompany_id()))
                .senderId(principal.getUser().getCompany())
                .recipientId(recipientCompany)
                .dueDate(requestInvoice.getDueDate())
                .status(InvoiceStatus.PENDING)
                .processingStatus(ProcessingStatusType.WAITING_STATUS)
                .amount(requestInvoice.getAmount())
                .createdDate(LocalDateTime.now())
                .createdBy(principal.getUser().getCompany().getCompanyName())
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

    public void updateInvoice(RequestUpdateInvoice request) {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Invoice invoice = invoiceRepository.findById(request.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Invoice Not Found"));

        if (principal.getUser().getCompany() == invoice.getRecipientId()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot Updating Invoice");

        invoice.setAmount(request.getAmount());
        invoice.setDueDate(request.getDueDate());
        invoice.setItemList(request.getItemList());
        invoice.setModifiedBy(principal.getUsername2());
        invoice.setModifiedDate(LocalDateTime.now());
        invoice.setProcessingStatus(ProcessingStatusType.RESOLVE_INVOICE);

        invoiceIssueLogRepository.deleteByInvoice(invoice);

        invoiceRepository.saveAndFlush(invoice);
    }

    @Transactional(readOnly = true)
    @Override
    public InvoiceDetailResponse getInvoiceDetail(String id) {
        //get current user login
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findUserByCredential(principal).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "User Not Found"));

        // get invoice by recipient and id
        Optional<Invoice> invoiceByRecipientIdAndInvoiceId = invoiceRepository.findInvoiceByRecipientIdAndInvoiceId(user.getCompany(), id);

        if (invoiceByRecipientIdAndInvoiceId.isEmpty()) {
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
    @Override
    public void updateInvoiceIssueLog(RequestInvoiceStatus requestInvoiceStatus) {
        invoiceRepository.findById(requestInvoiceStatus.getInvNumber()).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Data Not Found"));

        updateInvoiceStatus(requestInvoiceStatus);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public InvoiceDetailResponse updateInvoiceStatus(RequestInvoiceStatus requestInvoiceStatus) {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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

        if (invoice.getProcessingStatus().equals(ProcessingStatusType.REJECT_INVOICE) && processingStatusType.equals(ProcessingStatusType.APPROVE_INVOICE)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot Update Type From Reject To Approve.");
        }

        if (processingStatusType.equals(ProcessingStatusType.CANCEL_INVOICE)) {
            // cancel by seller
            invoice.setProcessingStatus(ProcessingStatusType.CANCEL_INVOICE);
            invoice.setStatus(InvoiceStatus.CANCELLED);
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
            invoice.setStatus(InvoiceStatus.DISPUTED);
        } else if (processingStatusType.equals(ProcessingStatusType.APPROVE_INVOICE)) {
            // approve
            invoice.setProcessingStatus(ProcessingStatusType.APPROVE_INVOICE);
            invoice.setStatus(InvoiceStatus.UNPAID);
        }
        invoice.setModifiedDate(LocalDateTime.now());
        invoice.setModifiedBy(principal.getUsername2());
        invoice.setProcessingStatus(processingStatusType);
        invoiceRepository.saveAndFlush(invoice);

        return mapToInvoiceDetailResponse(invoice);
    }

    private InvoiceDetailResponse mapToInvoiceDetailResponse(Invoice invoice) {
        // map json to string
        List<ItemList> itemLists = mapStringToJson(invoice.getItemList());

        CompanyResponse companySender = companyService.findById(invoice.getSenderId().getCompany_id());
        CompanyResponse companyRecipient = companyService.findById(invoice.getRecipientId().getCompany_id());

        if (companySender.getCompanyName() == null) {
            companySender.setCompanyName("Bank Danamon");
        }

        InvoiceIssueLog invoiceIssueLog = invoice.getInvoiceIssueLog();
        String issue = invoiceIssueLog == null ? null : invoiceIssueLog.getReason();
        String reason = invoiceIssueLog == null ? null : invoiceIssueLog.getIssueType().name();

        return InvoiceDetailResponse.builder()
                .companyFrom(companySender)
                .companyRecipient(companyRecipient)
                .invoiceId(invoice.getInvoiceId())
                .date(Date.from(invoice.getCreatedDate().atZone(ZoneId.systemDefault()).toInstant()))
                .dueDate(invoice.getDueDate())
                .processingStatus(invoice.getProcessingStatus().name())
                .itemList(itemLists)
                .issue(issue)
                .reason(reason)
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

        List<Company> companies = new ArrayList<>(user.getUserAccsess().stream().map(UserAccsess::getCompany).toList());
        companies.add(recipientCompany);

        boolean isSuperUser = principal.getRoles().stream()
                .anyMatch(role -> role.getRole().getRoleName().equals(RoleType.SUPER_USER.getName()));

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

            if (request.getType().equals("payable")) {
                column = "recipientId";
            }

            Predicate id = criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get(column)),
                    recipientCompany.getCompany_id().toLowerCase()
            );
            predicates.add(id);

            if (!isSuperUser) {
                if (request.getType().equals("payable")) {
                    predicates.add(root.get("senderId").in(companies));
                } else {
                    predicates.add(root.get("recipientId").in(companies));
                }
            }

            return query
                    .where(predicates.toArray(new Predicate[]{}))
                    .getRestriction();
        };

        Sort.Direction direction = Sort.Direction.fromString(request.getDirection());
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), direction, "status");
        Page<Invoice> invoices = invoiceRepository.findAll(specification, pageable);

        if (request.getType().equals("payable")) {
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
                .companyName(invoice.getSenderId().getCompanyName())
                .status(String.valueOf(invoice.getStatus()))
                .dueDate(invoice.getDueDate())
                .build();
    }

    private InvoiceResponse mapToResponseReceivable(Invoice invoice) {
        return InvoiceResponse.builder()
                .company_id(invoice.getRecipientId().getCompany_id())
                .invNumber(invoice.getInvoiceId())
                .amount(invoice.getAmount())
                .companyName(invoice.getRecipientId().getCompanyName())
                .status(String.valueOf(invoice.getStatus()))
                .dueDate(invoice.getDueDate())
                .issue(invoice.getInvoiceIssueLog())
                .build();
    }

    @Override
    public List<Invoice> getInvoiceByRecepientId(String id) {
        Company company = companyService.getById(id);

        return invoiceRepository.findAllByRecipientId(company);
    }

    @Override
    public List<Invoice> getInvoiceBySenderId(String id) {
        Company company = companyService.getById(id);

        return invoiceRepository.findAllBySenderId(company);
    }

    @Override
    public Long getTotalPaidInvoicePayable() {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Invoice> invoices = invoiceRepository.findAllByRecipientIdAndStatusOrStatus(principal.getUser().getCompany(), InvoiceStatus.PAID, InvoiceStatus.LATE_PAID);
        long total = 0;
        for (Invoice i : invoices) {
            total += i.getAmount();
        }
        return total;
    }

    @Override
    public Long getTotalUnpaidInvoicePayable() {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Invoice> invoices = invoiceRepository.findAllByRecipientIdAndStatusOrStatus(principal.getUser().getCompany(), InvoiceStatus.UNPAID, InvoiceStatus.LATE_UNPAID);
        long total = 0;
        for (Invoice i : invoices) {
            total += i.getAmount();
        }
        return total;
    }

    @Override
    public Long getTotalPaidInvoiceReceivable() {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Invoice> invoices = invoiceRepository.findAllBySenderIdAndStatusOrStatus(principal.getUser().getCompany(), InvoiceStatus.PAID, InvoiceStatus.LATE_PAID);
        long total = 0;
        for (Invoice i : invoices) {
            total += i.getAmount();
        }
        return total;
    }

    @Override
    public Long getTotalUnpaidInvoiceReceivable() {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Invoice> invoices = invoiceRepository.findAllBySenderIdAndStatusOrStatus(principal.getUser().getCompany(), InvoiceStatus.UNPAID, InvoiceStatus.LATE_UNPAID);
        long total = 0;
        for (Invoice i : invoices) {
            total += i.getAmount();
        }
        return total;
    }

    @Override
    public List<Invoice> getPaidBetweenCreatedDate(Company company, List<InvoiceStatus> statuses, LocalDateTime createdDate, LocalDateTime createdDate2) {
        return invoiceRepository.findAllByRecipientIdAndStatusInAndCreatedDateBetween(company, statuses, createdDate, createdDate2);
    }

    @Override
    public List<Invoice> getInvoiceApprove(Company company, ProcessingStatusType processingStatusType) {
        return invoiceRepository.findAllByRecipientIdAndProcessingStatusEquals(company, processingStatusType);
    }
}
