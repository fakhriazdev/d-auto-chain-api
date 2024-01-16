package com.danamon.autochain.service.impl;

import com.danamon.autochain.constant.RoleType;
import com.danamon.autochain.constant.payment.PaymentMethod;
import com.danamon.autochain.constant.payment.PaymentType;
import com.danamon.autochain.constant.invoice.InvoiceStatus;
import com.danamon.autochain.dto.Invoice.ItemList;
import com.danamon.autochain.dto.Invoice.response.InvoiceDetailResponse;
import com.danamon.autochain.dto.Invoice.response.InvoiceResponse;
import com.danamon.autochain.dto.financing.PayableDetailResponse;
import com.danamon.autochain.dto.payment.*;
import com.danamon.autochain.dto.user_dashboard.LimitResponse;
import com.danamon.autochain.entity.*;
import com.danamon.autochain.repository.FinancingPayableRepository;
import com.danamon.autochain.repository.PaymentRepository;
import com.danamon.autochain.repository.UserRepository;
import com.danamon.autochain.service.*;
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

import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final CompanyService companyService;
    private final InvoiceService invoiceService;
    private final FinancingPayableRepository financingPayableRepository;
    private final AuthService authService;

    @Override
    @Transactional
    public void createPayment(CreatePaymentRequest request) {
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
    public void deletePayment(Payment payment) {
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
                case "PARTIAL_FINANCING":
                    types.add(PaymentType.PARTIAL_FINANCING);
                    break;
                default:
                    types.addAll(Arrays.asList(PaymentType.INVOICING, PaymentType.FINANCING, PaymentType.PARTIAL_FINANCING));
                    break;
            }
        } else {
            types.addAll(Arrays.asList(PaymentType.INVOICING, PaymentType.FINANCING, PaymentType.PARTIAL_FINANCING));
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
        String paidDate = null;
        if(payment.getPaidDate() != null) paidDate = payment.getPaidDate().toString();

        String recipient = "";
        if(request != null) {
            if(request.getGroupBy().equals("payable")) {
                recipient = payment.getSenderId().getCompanyName();
            } else {
                recipient = payment.getRecipientId().getCompanyName();
            }
        } else {
            Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = userRepository.findUserByCredential(principal).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credential invalid"));

            recipient = Objects.equals(user.getCompany(), payment.getSenderId()) ? payment.getRecipientId().getCompanyName() : payment.getSenderId().getCompanyName();
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
                .paidDate(paidDate)
                .method(payment.getMethod().toString())
                .status(payment.getStatus().toString())
                .recepient(recipient)
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

    @Override
    public LimitResponse getLimitDashboard() {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findUserByCredential(principal).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credential invalid"));

        YearMonth currentMonth = YearMonth.now();

        Date currentMonthStartDate = java.sql.Date.valueOf(currentMonth.atDay(1));
        Date currentMonthEndDate = java.sql.Date.valueOf(currentMonth.atEndOfMonth());

        YearMonth lastMonth = currentMonth.minusMonths(1);

        Date lastMonthStartDate = java.sql.Date.valueOf(lastMonth.atDay(1));
        Date lastMonthEndDate = java.sql.Date.valueOf(lastMonth.atEndOfMonth());

        List<Payment> currentMonthIncome = paymentRepository.findAllBySenderIdAndDueDateBetween(user.getCompany(), currentMonthStartDate, currentMonthEndDate);
        Double sumCurrentMonthIncome = currentMonthIncome.stream()
                .mapToDouble(Payment::getAmount)
                .sum();

        List<Payment> lastMonthIncome = paymentRepository.findAllBySenderIdAndDueDateBetween(user.getCompany(), lastMonthStartDate, lastMonthEndDate);
        Double sumLastMonthIncome = lastMonthIncome.stream()
                .mapToDouble(Payment::getAmount)
                .sum();

        List<Payment> currentMonthExpense = paymentRepository.findAllByRecipientIdAndDueDateBetween(user.getCompany(), currentMonthStartDate, currentMonthEndDate);
        Double sumCurrentMonthExpense = currentMonthExpense.stream()
                .mapToDouble(Payment::getAmount)
                .sum();

        List<Payment> lastMonthExpense = paymentRepository.findAllByRecipientIdAndDueDateBetween(user.getCompany(), lastMonthStartDate, lastMonthEndDate);
        double sumLastMonthExpense = lastMonthExpense.stream()
                .mapToDouble(Payment::getAmount)
                .sum();

        return LimitResponse.builder()
                .limit(user.getCompany().getFinancingLimit())
                .limitUsed(user.getCompany().getFinancingLimit() - user.getCompany().getRemainingLimit())
                .income(sumCurrentMonthIncome)
                .incomeLastMonth(sumLastMonthIncome)
                .incomeDifferencePercentage(sumLastMonthIncome == 0 ? 0 : (sumCurrentMonthIncome - sumLastMonthIncome) / sumLastMonthIncome)
                .expense(sumCurrentMonthExpense)
                .expenseLastMonth(sumLastMonthExpense)
                .expenseDifferencePercentage(sumLastMonthExpense == 0 ? 0 : (sumCurrentMonthExpense - sumLastMonthExpense) / sumLastMonthExpense)
                .build();
    }

    @Override
    public PaymentDetailFinancing getPaymentDetailFinancing(Payment payment) {
        //get invoice
//        InvoiceDetailResponse invoiceDetail = invoiceService.getInvoiceDetail(payment.getInvoice().getInvoiceId());

        //get financing payable
//        PayableDetailResponse detailPayable = financingService.get_detail_payable(payment.getFinancingPayable().getFinancingPayableId());

        FinancingPayable financingPayable = financingPayableRepository.findByPayment(payment).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Data Not Found"));

        return PaymentDetailFinancing.builder()
                .transactionId(financingPayable.getInvoice().getInvoiceId())
                .tenor(financingPayable.getTenure().toString())
                .supplier(financingPayable.getCompany().getCompanyName())
                .amount(financingPayable.getAmount())
                .paymentMethod(financingPayable.getPayment().getMethod().name())
                .financingId(financingPayable.getFinancingPayableId())
                .build();

    }

    @Override
    public InvoiceDetailResponse getPaymentDetailInvoice(Payment payment) {
        return invoiceService.getInvoiceDetail(payment.getInvoice().getInvoiceId());
    }

    @Override
    public Payment getPaymentDetailType(String paymentId) {
        return paymentRepository.findById(paymentId).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Data Not Found"));
    }
}
