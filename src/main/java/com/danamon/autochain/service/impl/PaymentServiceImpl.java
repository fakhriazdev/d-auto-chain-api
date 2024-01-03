package com.danamon.autochain.service.impl;


import com.danamon.autochain.constant.PaymentMethod;
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
//    public List<PaymentResponse> getOngoingPayments(SearchPaymentRequest request) {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findUserByCredential(principal).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credential invalid"));
        // get invoices
        List<Invoice> invoices = invoiceService.getInvoiceByRecepientId(user.getCompany().getCompany_id());

        Sort.Direction direction = Sort.Direction.fromString(request.getDirection());
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), direction, "outstandingFlag", "type");
        // get payments by invoice
//        List<Payment> payments = paymentRepository.findAllByInvoiceInAndOutstandingFlagIn(invoices, Arrays.asList(Status.UNPAID, Status.LATE_UNPAID));
        Page<Payment> payments = paymentRepository.findAllByInvoiceInAndOutstandingFlagIn(invoices, Arrays.asList(Status.UNPAID, Status.LATE_UNPAID), pageable);
        //        Specification<Invoice> specification = (root, query, criteriaBuilder) -> {
//            List<Predicate> predicates = new ArrayList<>();
//
//            if (request.getStatus() != null) {
//                Predicate status = criteriaBuilder.equal(
//                        criteriaBuilder.lower(root.get("status")),
//                        request.getStatus().toLowerCase()
//                );
//                predicates.add(status);
//            }
//
//            String column = "senderId";
//            assert request.getType() != null;
//
//            if(request.getType().equals("payable")){
//                column = "recipientId";
//            }
//
//            Predicate id = criteriaBuilder.equal(
//                    criteriaBuilder.lower(root.get(column)),
//                    recipientCompany.getCompany_id().toLowerCase()
//            );
//            predicates.add(id);
//
//            return query
//                    .where(predicates.toArray(new Predicate[]{}))
//                    .getRestriction();
//        };
//
//        Sort.Direction direction = Sort.Direction.fromString(request.getDirection());
//        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), direction , "status");
//        Page<Invoice> invoices = invoiceRepository.findAll(specification, pageable);
//
//        if(request.getType().equals("payable")){
//            return invoices.map(this::mapToResponsePayable);
//        } else {
//            return invoices.map(this::mapToResponseReceivable);
//        }

//        return payments.stream().map(payment -> mapToResponsePayment(payment)).collect(Collectors.toList());
        return payments.map(payment -> mapToResponsePayment(payment));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getHistoryPayments(SearchPaymentRequest request) {
//    public List<PaymentResponse> getHistoryPayments(SearchPaymentRequest request) {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findUserByCredential(principal).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credential invalid"));
        // get invoices
        List<Invoice> invoices = invoiceService.getInvoiceByRecepientId(user.getCompany().getCompany_id());

        Sort.Direction direction = Sort.Direction.fromString(request.getDirection());
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), direction, "outstandingFlag", "type");
        // get payments by invoice
//        List<Payment> payments = paymentRepository.findAllByInvoiceInAndOutstandingFlagIn(invoices, Arrays.asList(Status.PAID, Status.LATE_PAID));
        Page<Payment> payments = paymentRepository.findAllByInvoiceInAndOutstandingFlagIn(invoices, Arrays.asList(Status.PAID, Status.LATE_PAID), pageable);

        //        Specification<Invoice> specification = (root, query, criteriaBuilder) -> {
//            List<Predicate> predicates = new ArrayList<>();
//
//            if (request.getStatus() != null) {
//                Predicate status = criteriaBuilder.equal(
//                        criteriaBuilder.lower(root.get("status")),
//                        request.getStatus().toLowerCase()
//                );
//                predicates.add(status);
//            }
//
//            String column = "senderId";
//            assert request.getType() != null;
//
//            if(request.getType().equals("payable")){
//                column = "recipientId";
//            }
//
//            Predicate id = criteriaBuilder.equal(
//                    criteriaBuilder.lower(root.get(column)),
//                    recipientCompany.getCompany_id().toLowerCase()
//            );
//            predicates.add(id);
//
//            return query
//                    .where(predicates.toArray(new Predicate[]{}))
//                    .getRestriction();
//        };
//
//        Sort.Direction direction = Sort.Direction.fromString(request.getDirection());
//        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), direction , "status");
//        Page<Invoice> invoices = invoiceRepository.findAll(specification, pageable);
//
//        if(request.getType().equals("payable")){
//            return invoices.map(this::mapToResponsePayable);
//        } else {
//            return invoices.map(this::mapToResponseReceivable);
//        }

//        return payments.stream().map(payment -> mapToResponsePayment(payment)).collect(Collectors.toList());
        return payments.map(payment -> mapToResponsePayment(payment));
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
                .dueDate(payment.getDueDate())
                .paidDate(payment.getPaidDate())
                .method(payment.getMethod().toString())
                .source(payment.getSource())
                .outstandingFlag(payment.getOutstandingFlag().toString())
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
