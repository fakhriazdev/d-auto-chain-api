package com.danamon.autochain.service.impl;


import com.danamon.autochain.constant.PaymentMethod;
import com.danamon.autochain.constant.PaymentType;
import com.danamon.autochain.constant.invoice.ProcessingStatusType;
import com.danamon.autochain.constant.invoice.ReasonType;
import com.danamon.autochain.constant.invoice.Status;
import com.danamon.autochain.dto.Invoice.ItemList;
import com.danamon.autochain.dto.Invoice.request.RequestInvoice;
import com.danamon.autochain.dto.Invoice.request.RequestInvoiceStatus;
import com.danamon.autochain.dto.Invoice.request.SearchInvoiceRequest;
import com.danamon.autochain.dto.Invoice.response.InvoiceDetailResponse;
import com.danamon.autochain.dto.Invoice.response.InvoiceResponse;
import com.danamon.autochain.dto.company.CompanyResponse;
import com.danamon.autochain.dto.payment.PaymentChangeMethodRequest;
import com.danamon.autochain.dto.payment.PaymentResponse;
import com.danamon.autochain.dto.payment.SearchPaymentRequest;
import com.danamon.autochain.entity.*;
import com.danamon.autochain.repository.InvoiceIssueLogRepository;
import com.danamon.autochain.repository.InvoiceRepository;
import com.danamon.autochain.repository.PaymentRepository;
import com.danamon.autochain.repository.UserRepository;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.service.InvoiceService;
import com.danamon.autochain.service.PaymentService;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final InvoiceService invoiceService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getOngoingPayments(SearchPaymentRequest request) {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findUserByCredential(principal).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credential invalid"));
        // get invoices
        List<Invoice> invoices;
        if (request.getGroupBy().equals("payable")) {
            invoices = invoiceService.getInvoiceByRecepientId(user.getCompany().getCompany_id());
        } else {
            invoices = invoiceService.getInvoiceBySenderId(user.getCompany().getCompany_id());
        }

        Sort.Direction direction = Sort.Direction.fromString(request.getDirection());
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), direction, "status", "type");

        // get payments by invoice
        List<Status> statuses = getOngoingStatuses(request);
        List<PaymentType> types = getTypes(request);

        Page<Payment> payments = paymentRepository.findAll(
                withInvoiceAndStatus(invoices, statuses, types, request.getTransactionId()),
                pageable
        );

        return payments.map(payment -> mapToResponsePayment(payment));
    }

    private static List<PaymentType> getTypes(SearchPaymentRequest request) {
        List<PaymentType> types = new ArrayList<>();
        if (request.getType() != null) {
            switch (request.getType()) {
                case "INVOICING":
                    types.add(PaymentType.INVOICING);
                    break;
                case "FINANCING":
                    types.add(PaymentType.FINANCING);
                    break;
                default:
                    types.addAll(Arrays.asList(PaymentType.INVOICING, PaymentType.FINANCING));
                    break;
            }
        } else {
            types.addAll(Arrays.asList(PaymentType.INVOICING, PaymentType.FINANCING));
        }

        return types;
    }

    private static List<Status> getOngoingStatuses(SearchPaymentRequest request) {
        List<Status> statuses = new ArrayList<>();
        if (request.getStatus() != null) {
            switch (request.getStatus()) {
                case "UNPAID":
                    statuses.add(Status.UNPAID);
                    break;
                case "LATE_UNPAID":
                    statuses.add(Status.LATE_UNPAID);
                    break;
                default:
                    statuses.addAll(Arrays.asList(Status.UNPAID, Status.LATE_UNPAID));
                    break;
            }
        } else {
            statuses.addAll(Arrays.asList(Status.UNPAID, Status.LATE_UNPAID));
        }

        return statuses;
    }

    public static Specification<Payment> withInvoiceAndStatus(List<Invoice> invoices, List<Status> statuses, List<PaymentType> types, String transactionId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(root.get("invoice").in(invoices));
            predicates.add(root.get("status").in(statuses));
            predicates.add(root.get("type").in(types));

            if (transactionId != null) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("transactionId")),
                        "%" + transactionId.toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getHistoryPayments(SearchPaymentRequest request) {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findUserByCredential(principal).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credential invalid"));
        // get invoices by groupBy
        List<Invoice> invoices = invoiceService.getInvoiceByRecepientId(user.getCompany().getCompany_id());

        Sort.Direction direction = Sort.Direction.fromString(request.getDirection());
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), direction, "status", "type");

        // get payments by invoice
        List<Status> statuses = getHistoryStatuses(request);
        List<PaymentType> types = getTypes(request);

        Page<Payment> payments = paymentRepository.findAll(
                withInvoiceAndStatus(invoices, statuses, types, request.getTransactionId()),
                pageable
        );

        return payments.map(payment -> mapToResponsePayment(payment));
    }

    private static List<Status> getHistoryStatuses(SearchPaymentRequest request) {
        List<Status> statuses = new ArrayList<>();
        if (request.getStatus() != null) {
            switch (request.getStatus()) {
                case "PAID":
                    statuses.add(Status.PAID);
                    break;
                case "LATE_PAID":
                    statuses.add(Status.LATE_PAID);
                    break;
                default:
                    statuses.addAll(Arrays.asList(Status.PAID, Status.LATE_PAID));
                    break;
            }
        } else {
            statuses.addAll(Arrays.asList(Status.PAID, Status.LATE_PAID));
        }

        return statuses;
    }

    private PaymentResponse mapToResponsePayment(Payment payment) {
        List<ItemList> itemLists;

        try {
            itemLists = objectMapper.readValue(payment.getInvoice().getItemList(), new TypeReference<List<ItemList>>() {});
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while converting string to JSON. Please contact administrator");
        }

        PaymentResponse paymentResponse = PaymentResponse.builder()
                .transactionId(payment.getTransactionId())
                .invoice(
                        InvoiceResponse.builder()
                                .company_id(payment.getInvoice().getRecipientId().getCompany_id())
                        .companyName(payment.getInvoice().getRecipientId().getCompanyName())
                        .status(String.valueOf(payment.getInvoice().getStatus()))
                        .invNumber(payment.getInvoice().getInvoiceId())
                        .dueDate(payment.getInvoice().getDueDate())
                        .amount(payment.getInvoice().getAmount())
                        .itemList(itemLists)
                        .build()
                )
                .amount(payment.getAmount())
                .type(payment.getType().toString())
                .dueDate(payment.getDueDate().toString())
                .paidDate(payment.getPaidDate().toString())
                .method(payment.getMethod().toString())
                .status(payment.getStatus().toString())
                .build();

        return paymentResponse;
    }

    @Override
    public PaymentResponse changeMethodPayment(PaymentChangeMethodRequest request) {
        Payment paymentOld = paymentRepository.findById(request.getTransactionId()).orElseThrow(() -> new RuntimeException("payment not found"));

        paymentOld.setMethod(PaymentMethod.valueOf(request.getMethod()));

        Payment payment = paymentRepository.saveAndFlush(paymentOld);

        return mapToResponsePayment(payment);
    }
}
