package com.danamon.autochain.service.impl;


import com.danamon.autochain.constant.PaymentMethod;
import com.danamon.autochain.constant.PaymentType;
import com.danamon.autochain.constant.invoice.Status;
import com.danamon.autochain.dto.Invoice.ItemList;
import com.danamon.autochain.dto.Invoice.response.InvoiceResponse;
import com.danamon.autochain.dto.payment.CreatePaymentRequest;
import com.danamon.autochain.dto.payment.PaymentChangeMethodRequest;
import com.danamon.autochain.dto.payment.PaymentResponse;
import com.danamon.autochain.dto.payment.SearchPaymentRequest;
import com.danamon.autochain.entity.*;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final CompanyService companyService;

    @Override
    @Transactional
    public void createPayment(CreatePaymentRequest request){

    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getOngoingPayments(SearchPaymentRequest request) {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findUserByCredential(principal).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credential invalid"));

        Sort.Direction direction = Sort.Direction.fromString(request.getDirection());
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), direction, "status", "type");

        List<Status> statuses = getOngoingStatuses(request);
        List<PaymentType> types = getTypes(request);
        List<Company> recipients = request.getRecipient() != null ? companyService.getCompaniesNameLike(request.getRecipient()) : null;

        Page<Payment> payments = paymentRepository.findAll(
                withInvoiceAndStatus(user, statuses, types, request.getGroupBy(), recipients),
                pageable
        );

        return payments.map(payment -> mapToResponsePayment(payment, request));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getHistoryPayments(SearchPaymentRequest request) {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findUserByCredential(principal).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credential invalid"));

        Sort.Direction direction = Sort.Direction.fromString(request.getDirection());
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), direction, "status", "type");

        List<Status> statuses = getHistoryStatuses(request);
        List<PaymentType> types = getTypes(request);
        List<Company> recipients = request.getRecipient() != null ? companyService.getCompaniesNameLike(request.getRecipient()) : null;

        Page<Payment> payments = paymentRepository.findAll(
                withInvoiceAndStatus(user, statuses, types, request.getGroupBy(), recipients),
                pageable
        );

        return payments.map(payment -> mapToResponsePayment(payment, request));
    }

    private static Specification<Payment> withInvoiceAndStatus(User user, List<Status> statuses, List<PaymentType> types, String groupBy, List<Company> recipients) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (groupBy.equals("payable")) {
                predicates.add(criteriaBuilder.equal(
                        root.get("recipientId"),
                        user.getCompany()
                ));

                if (recipients != null) {
                    predicates.add(root.get("senderId").in(recipients));
                }
            } else {
                predicates.add(criteriaBuilder.equal(
                        root.get("senderId"),
                        user.getCompany()
                ));

                if (recipients != null) {
                    predicates.add(root.get("recipientId").in(recipients));
                }
            }

            predicates.add(root.get("status").in(statuses));
            predicates.add(root.get("type").in(types));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
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

    private PaymentResponse mapToResponsePayment(Payment payment, SearchPaymentRequest request) {
        List<ItemList> itemLists;

        try {
            itemLists = objectMapper.readValue(payment.getInvoice().getItemList(), new TypeReference<List<ItemList>>() {
            });
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while converting string to JSON. Please contact administrator");
        }

        PaymentResponse paymentResponse = PaymentResponse.builder()
                .transactionId(payment.getPaymentId())
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
                .recepient(request.getGroupBy().equals("payable") ? payment.getSenderId().getCompanyName() : payment.getRecipientId().getCompanyName())
                .build();

        return paymentResponse;
    }

    @Override
    public PaymentResponse changeMethodPayment(PaymentChangeMethodRequest request) {
        Payment paymentOld = paymentRepository.findById(request.getTransactionId()).orElseThrow(() -> new RuntimeException("payment not found"));

        paymentOld.setMethod(PaymentMethod.valueOf(request.getMethod()));

        Payment payment = paymentRepository.saveAndFlush(paymentOld);

        return mapToResponsePayment(payment, null);
    }
}
