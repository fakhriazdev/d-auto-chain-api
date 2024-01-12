package com.danamon.autochain.service.impl;

import com.danamon.autochain.constant.RoleType;
import com.danamon.autochain.constant.payment.PaymentMethod;
import com.danamon.autochain.constant.payment.PaymentType;
import com.danamon.autochain.constant.invoice.InvoiceStatus;
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
        Payment payment = Payment.builder()
                .amount(request.getAmount())
                .dueDate(request.getDueDate())
                .invoice(request.getInvoice())
                .recipientId(request.getRecipientId())
                .senderId(request.getSenderId())
                .type(request.getType())
                .status(request.getStatus())
                .method(request.getMethod())
                .build();
        paymentRepository.saveAndFlush(payment);
    }

    @Override
    public void deletePayment(Payment payment){
        paymentRepository.delete(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getOngoingPayments(SearchPaymentRequest request) {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findUserByCredential(principal).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credential invalid"));

        Sort.Direction direction = Sort.Direction.fromString(request.getDirection());
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), direction, "status", "type");

        List<InvoiceStatus> invoiceStatuses = getOngoingStatuses(request);
        List<PaymentType> types = getTypes(request);
        List<Company> recipients = request.getRecipient() != null ? companyService.getCompaniesNameLike(request.getRecipient()) : null;

        List<Company> companies = new ArrayList<>(user.getUserAccsess().stream().map(UserAccsess::getCompany).toList());

        boolean isSuperUser = principal.getRoles().stream()
                .anyMatch(role -> role.getRole().getRoleName().equals(RoleType.SUPER_USER.getName()));

        Page<Payment> payments = paymentRepository.findAll(
                withInvoiceAndStatus(user, invoiceStatuses, types, request.getGroupBy(), recipients, isSuperUser, companies),
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

        List<InvoiceStatus> invoiceStatuses = getHistoryStatuses(request);
        List<PaymentType> types = getTypes(request);
        List<Company> recipients = request.getRecipient() != null ? companyService.getCompaniesNameLike(request.getRecipient()) : null;

        List<Company> companies = new ArrayList<>(user.getUserAccsess().stream().map(UserAccsess::getCompany).toList());

        boolean isSuperUser = principal.getRoles().stream()
                .anyMatch(role -> role.getRole().getRoleName().equals(RoleType.SUPER_USER.getName()));

        Page<Payment> payments = paymentRepository.findAll(
                withInvoiceAndStatus(user, invoiceStatuses, types, request.getGroupBy(), recipients, isSuperUser, companies),
                pageable
        );

        return payments.map(payment -> mapToResponsePayment(payment, request));
    }

    private static Specification<Payment> withInvoiceAndStatus(User user, List<InvoiceStatus> invoiceStatuses, List<PaymentType> types, String groupBy, List<Company> recipients, boolean isSuperUser, List<Company> accessCompanies) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (groupBy.equals("payable")) {
                predicates.add(criteriaBuilder.equal(
                        root.get("recipientId"),
                        user.getCompany()
                ));

                if (!isSuperUser) {
                    predicates.add(root.get("senderId").in(accessCompanies));
                }

                if (recipients != null) {
                    predicates.add(root.get("senderId").in(recipients));
                }
            } else {
                predicates.add(criteriaBuilder.equal(
                        root.get("senderId"),
                        user.getCompany()
                ));

                if (!isSuperUser) {
                    predicates.add(root.get("senderId").in(accessCompanies));
                }

                if (recipients != null) {
                    predicates.add(root.get("recipientId").in(recipients));
                }
            }

            predicates.add(root.get("status").in(invoiceStatuses));
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

    private static List<InvoiceStatus> getOngoingStatuses(SearchPaymentRequest request) {
        List<InvoiceStatus> invoiceStatuses = new ArrayList<>();
        if (request.getStatus() != null) {
            switch (request.getStatus()) {
                case "UNPAID":
                    invoiceStatuses.add(InvoiceStatus.UNPAID);
                    break;
                case "LATE_UNPAID":
                    invoiceStatuses.add(InvoiceStatus.LATE_UNPAID);
                    break;
                default:
                    invoiceStatuses.addAll(Arrays.asList(InvoiceStatus.UNPAID, InvoiceStatus.LATE_UNPAID));
                    break;
            }
        } else {
            invoiceStatuses.addAll(Arrays.asList(InvoiceStatus.UNPAID, InvoiceStatus.LATE_UNPAID));
        }

        return invoiceStatuses;
    }

    private static List<InvoiceStatus> getHistoryStatuses(SearchPaymentRequest request) {
        List<InvoiceStatus> invoiceStatuses = new ArrayList<>();
        if (request.getStatus() != null) {
            switch (request.getStatus()) {
                case "PAID":
                    invoiceStatuses.add(InvoiceStatus.PAID);
                    break;
                case "LATE_PAID":
                    invoiceStatuses.add(InvoiceStatus.LATE_PAID);
                    break;
                default:
                    invoiceStatuses.addAll(Arrays.asList(InvoiceStatus.PAID, InvoiceStatus.LATE_PAID));
                    break;
            }
        } else {
            invoiceStatuses.addAll(Arrays.asList(InvoiceStatus.PAID, InvoiceStatus.LATE_PAID));
        }

        return invoiceStatuses;
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
                                .status(String.valueOf(payment.getInvoice().getInvoiceStatus()))
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
