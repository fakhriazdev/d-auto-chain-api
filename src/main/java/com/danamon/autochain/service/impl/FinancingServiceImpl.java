package com.danamon.autochain.service.impl;

import com.danamon.autochain.constant.TenureStatus;
import com.danamon.autochain.constant.financing.FinancingStatus;
import com.danamon.autochain.constant.financing.FinancingType;
import com.danamon.autochain.constant.payment.PaymentStatus;
import com.danamon.autochain.constant.payment.PaymentType;
import com.danamon.autochain.dto.financing.*;
import com.danamon.autochain.dto.transaction.TransactionRequest;
import com.danamon.autochain.entity.*;
import com.danamon.autochain.repository.*;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.service.FinancingService;
import com.danamon.autochain.service.PaymentService;
import com.danamon.autochain.service.TransactionService;
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
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FinancingServiceImpl implements FinancingService {
    private final FinancingReceivableRepository financingReceivableRepository;
    private final FinancingPayableRepository financingPayableRepository;
    private final TenureRepository tenureRepository;
    private final InvoiceRepository invoiceRepository;
    private final CompanyRepository companyRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final CompanyService companyService;
    private final PaymentService paymentService;
    private final TransactionService transactionService;

    @Override
    public Map<String, Double> get_limit() {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findUserByCredential(principal).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credential invalid"));
        Company company = companyService.getById(user.getCompany().getCompany_id());

        Map<String, Double> data = new HashMap<>();
        data.put("financing_limit", company.getFinancingLimit());
        data.put("remaining_limit", company.getRemainingLimit());
        return data;
    }

//    =================================== FINANCING PAYABLE ==========================================

    @Override
    public void create_financing_payable(List<PayableRequest> requests) {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findUserByCredential(principal).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credential invalid"));
        Company company = companyRepository.findById(user.getCompany().getCompany_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "invalid company id"));

        requests.forEach(request -> {

            if (request.getAmount() < 75000000) throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Amount cannot low than Rp.75.000.000");

            double interest = 0.01d;

            long loanAmount = request.getAmount();

            if (loanAmount >= 75000000 && loanAmount <= 500000000L) {
                interest = 0.07d;
            } else if (loanAmount <= 1000000000L) {
                interest = 0.075d;
            } else if (loanAmount <= 1500000000L) {
                interest = 0.08d;
            } else if (loanAmount <= 2000000000L) {
                interest = 0.085d;
            } else if (loanAmount <= 2500000000L) {
                interest = 0.09d;
            }

//          ======== FORMULA =======
            double paymentPermount = loanAmount * (
                    (interest * Math.pow((1 + interest), request.getTenure())) /
                            ((Math.pow(1 + interest, request.getTenure())) - 1)
            );

            Payment payment = paymentRepository.findById(request.getPayment_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid payment id : " + request.getPayment_id()));
            financingPayableRepository.saveAndFlush(
                    FinancingPayable.builder()
                            .company(company)
                            .invoice(payment.getInvoice())
                            .createdBy(user.getName())
                            .createdDate(LocalDateTime.now())
                            .amount(request.getAmount())
                            .installments_number(request.getInstallments_number())
                            .interest(loanAmount * interest)
                            .total(loanAmount + (loanAmount * interest))
                            .period_number(0)
                            .tenure(paymentPermount)
                            .status(FinancingStatus.PENDING)
                            .build()
            );
        });
    }

    @Override
    public Page<FinancingResponse> get_all_payable(SearchFinancingRequest request) {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findUserByCredential(principal).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credential invalid"));
        Company company = companyRepository.findById(user.getCompany().getCompany_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "invalid company id"));

        Specification<FinancingPayable> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getStatus() != null) {
                Predicate status = criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("status")),
                        request.getStatus().toLowerCase()
                );
                predicates.add(status);
            }

            Predicate id = criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("company")),
                    company.getCompany_id().toLowerCase()
            );
            predicates.add(id);

            return query
                    .where(predicates.toArray(new Predicate[]{}))
                    .getRestriction();
        };
        Sort.Direction direction = Sort.Direction.fromString(request.getDirection());
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), direction, "status");
        Page<FinancingPayable> financing = financingPayableRepository.findAll(specification, pageable);

        return financing.map(this::mapToResponsePayable);
    }

    @Override
    public PayableDetailResponse get_detail_payable(String financing_id) {
        FinancingPayable financingPayable = financingPayableRepository.findById(financing_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "invalid financing id"));
        List<Tenure> tenures = tenureRepository.findAllByfinancingPayableId(financingPayable);
        Map<String, String> sender = new HashMap<>();
        sender.put("company_name", financingPayable.getInvoice().getSenderId().getCompanyName());
        sender.put("email", financingPayable.getInvoice().getSenderId().getCompanyEmail());
        sender.put("phone_number", financingPayable.getInvoice().getSenderId().getPhoneNumber());
        sender.put("city", financingPayable.getInvoice().getSenderId().getCity());
        sender.put("province", financingPayable.getInvoice().getSenderId().getProvince());

        Map<String, String> recipient = new HashMap<>();
        recipient.put("company_name", financingPayable.getInvoice().getRecipientId().getCompanyName());
        recipient.put("email", financingPayable.getInvoice().getRecipientId().getCompanyEmail());
        recipient.put("phone_number", financingPayable.getInvoice().getRecipientId().getPhoneNumber());
        recipient.put("city", financingPayable.getInvoice().getRecipientId().getCity());
        recipient.put("province", financingPayable.getInvoice().getRecipientId().getProvince());

        List<TenureDetailResponse> listTenure = new ArrayList<>();
        tenures.forEach(tenure -> {
            listTenure.add(TenureDetailResponse.builder()
                    .tenure_id(tenure.getTenureId())
                    .due_date(tenure.getDueDate())
                    .amount(tenure.getAmount())
                    .status(tenure.getStatus().name())
                    .build());
        });
        return PayableDetailResponse.builder()
                .payment_number(financingPayable.getPayment().getPaymentId())
                .invoice_number(financingPayable.getInvoice().getInvoiceId())
                .recipient(recipient)
                .sender(sender)
                .total_amount(financingPayable.getAmount())
                .created_date(Date.from(financingPayable.getCreatedDate().atZone(ZoneId.systemDefault()).toInstant()))
                .tenure_list(listTenure)
                .build();
    }

    //   ===================================== FINANCING RECEIVABLE ==========================================
    @Override
    public void create_financing_receivable(List<ReceivableRequest> request) {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findUserByCredential(principal).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credential invalid"));
        Company company = companyRepository.findById(user.getCompany().getCompany_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "invalid company id"));

        List<FinancingReceivable> financingReceivables = new ArrayList<>();
        request.forEach(receivableRequest -> {

            if (receivableRequest.getAmount() <= 75000000) {
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Amount cannot less than Rp.75.000.000");
            }

            Invoice invoice = invoiceRepository.findById(receivableRequest.getInvoice_number())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid invoice id"));

            LocalDate current_date = LocalDate.now();
            LocalDate disbursement_date = receivableRequest.getDisbursment_date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate due_date = invoice.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            long daysDifference_disbursement = java.time.temporal.ChronoUnit.DAYS.between(current_date, disbursement_date);
            long daysDifference_dueDate = java.time.temporal.ChronoUnit.DAYS.between(current_date, due_date);
            System.out.println(daysDifference_dueDate + " - " + daysDifference_disbursement);

            // Formula
            long daysDifference = daysDifference_dueDate - daysDifference_disbursement;
            double discount = 0.07d;
            double fee = receivableRequest.getAmount() * discount * (daysDifference / 360d);
            double result = receivableRequest.getAmount() - fee;

            // Format the result with %.2f
            BigDecimal bigDecimal = new BigDecimal(result);
            BigDecimal roundedBigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
            double formattedResult = roundedBigDecimal.doubleValue();

            financingReceivables.add(
                    FinancingReceivable.builder()
                            .invoice(invoice)
                            .company(company)
                            .status(FinancingStatus.PENDING)
                            .financingType(FinancingType.RECEIVABLE)
                            .amount(receivableRequest.getAmount())
                            .fee(fee)
                            .total(formattedResult)
                            .disbursment_date(receivableRequest.getDisbursment_date())
                            .createdDate(LocalDateTime.now())
                            .createdBy(user.getName())
                            .build()
            );
        });
        financingReceivableRepository.saveAllAndFlush(financingReceivables);
    }

    @Override
    public Page<FinancingResponse> get_all_receivable(SearchFinancingRequest request) {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findUserByCredential(principal).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credential invalid"));
        Company company = companyRepository.findById(user.getCompany().getCompany_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "invalid company id"));

        Specification<FinancingReceivable> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getStatus() != null) {
                Predicate status = criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("status")),
                        request.getStatus().toLowerCase()
                );
                predicates.add(status);
            }

            Predicate id = criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("company")),
                    company.getCompany_id().toLowerCase()
            );
            predicates.add(id);

            return query
                    .where(predicates.toArray(new Predicate[]{}))
                    .getRestriction();
        };

        Sort.Direction direction = Sort.Direction.fromString(request.getDirection());
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), direction, "status");
        Page<FinancingReceivable> financing = financingReceivableRepository.findAll(specification, pageable);

        return financing.map(this::mapToResponseReceivable);
    }

    @Override
    public ReceivableDetailResponse get_detail_receivable(String financing_id) {
        FinancingReceivable financingReceivable = financingReceivableRepository.findById(financing_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "invalid financing id"));
        Map<String, String> sender = new HashMap<>();
        sender.put("company_name", financingReceivable.getInvoice().getSenderId().getCompanyName());
        sender.put("email", financingReceivable.getInvoice().getSenderId().getCompanyEmail());
        sender.put("phone_number", financingReceivable.getInvoice().getSenderId().getPhoneNumber());
        sender.put("city", financingReceivable.getInvoice().getSenderId().getCity());
        sender.put("province", financingReceivable.getInvoice().getSenderId().getProvince());

        Map<String, String> recipient = new HashMap<>();
        recipient.put("company_name", financingReceivable.getInvoice().getRecipientId().getCompanyName());
        recipient.put("email", financingReceivable.getInvoice().getRecipientId().getCompanyEmail());
        recipient.put("phone_number", financingReceivable.getInvoice().getRecipientId().getPhoneNumber());
        recipient.put("city", financingReceivable.getInvoice().getRecipientId().getCity());
        recipient.put("province", financingReceivable.getInvoice().getRecipientId().getProvince());

        return ReceivableDetailResponse.builder()
                .invoice_number(financingReceivable.getInvoice().getInvoiceId())
                .recipient(recipient)
                .sender(sender)
                .amount(financingReceivable.getAmount())
                .Fee(financingReceivable.getFee())
                .total(financingReceivable.getTotal())
                .created_date(financingReceivable.getDisbursment_date())
                .build();
    }


//    ====================================== BACK OFFICE ===========================================

    @Override
    public Page<FinancingResponse> backoffice_get_all_financing(SearchFinancingRequest request) {

        if (request.getType().equalsIgnoreCase("receivable")) {
            Specification<FinancingReceivable> specification = (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();

                if (request.getStatus() != null) {
                    Predicate status = criteriaBuilder.equal(
                            criteriaBuilder.lower(root.get("status")),
                            request.getStatus().toLowerCase()
                    );
                    predicates.add(status);
                }

                return query
                        .where(predicates.toArray(new Predicate[]{}))
                        .getRestriction();
            };

            Sort.Direction direction = Sort.Direction.fromString(request.getDirection());
            Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), direction, "status");
            Page<FinancingReceivable> financing = financingReceivableRepository.findAll(specification, pageable);

            return financing.map(this::mapToResponseReceivable);

        } else if (request.getType().equalsIgnoreCase("payable")) {
            Specification<FinancingPayable> specification = (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();

                if (request.getStatus() != null) {
                    Predicate status = criteriaBuilder.equal(
                            criteriaBuilder.lower(root.get("status")),
                            request.getStatus().toLowerCase()
                    );
                    predicates.add(status);
                }

                return query
                        .where(predicates.toArray(new Predicate[]{}))
                        .getRestriction();
            };

            Sort.Direction direction = Sort.Direction.fromString(request.getDirection());
            Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), direction, "status");
            Page<FinancingPayable> financing = financingPayableRepository.findAll(specification, pageable);

            return financing.map(this::mapToResponsePayable);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid status with : " + request.getStatus());
    }

    @Override
    public AcceptResponse backoffice_accept(AcceptRequest request) {
        if (request.getType().equalsIgnoreCase("payable")) {
            FinancingPayable financingPayable = financingPayableRepository.findById(request.getFinancing_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid Financial Id"));

            Double tenure_amount = (double) (financingPayable.getAmount() / financingPayable.getInstallments_number());

            for (int i = 1; i <= financingPayable.getInstallments_number(); i++) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.MONTH, i);
                Date dueDate = calendar.getTime();

                // Set the tenure status
                TenureStatus tenureStatus = (i > 1) ? TenureStatus.UPCOMING : TenureStatus.ONGOING;

                tenureRepository.saveAndFlush(
                        Tenure.builder()
                                .financingPayableId(financingPayable)
                                .dueDate(dueDate)
                                .status(tenureStatus)
                                .Amount(tenure_amount)
                                .build()
                );
            }

            paymentService.deletePayment(financingPayable.getPayment());
            financingPayable.setStatus(FinancingStatus.ONGOING);
            financingPayableRepository.saveAndFlush(financingPayable);
            return AcceptResponse.builder().build();

        } else if (request.getType().equalsIgnoreCase("receivable")) {
            FinancingReceivable financingReceivable = financingReceivableRepository.findById(request.getFinancing_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid Financial Id"));
            Payment payment = financingReceivable.getInvoice().getPayment();

            Long financing_amount = financingReceivable.getAmount();
            Long invoice_amount = financingReceivable.getInvoice().getAmount();
            Long payable_amount = invoice_amount - financing_amount;

            if (payable_amount < 0 || financing_amount > invoice_amount) {
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid financing request amount");
            }

            PaymentType partialFinancing = PaymentType.PARTIAL_FINANCING;
            AcceptDetailResponse buyyer = new AcceptDetailResponse();

            if (payable_amount == 0) {
                paymentService.deletePayment(payment);
                partialFinancing = PaymentType.FINANCING;
            }

//            buat partial payment danamon & seller
            if (financing_amount < invoice_amount) {
                payment.setAmount(payable_amount);
                payment.setType(PaymentType.PARTIAL_FINANCING);
                paymentRepository.saveAndFlush(payment);

                transactionService.createTransaction(
                        TransactionRequest.builder()
                                .recipientId(financingReceivable.getInvoice().getRecipientId())
                                .amount(payable_amount)
                                .createdDate(new Date())
                                .paymentStatus(PaymentStatus.COMPLETED)
                                .financingType(FinancingType.PAYABLE)
                                .build()
                );

                buyyer.setFinancingType(FinancingType.PAYABLE.name());
                buyyer.setAmountPaid(payable_amount);
                buyyer.setCompany_name(financingReceivable.getInvoice().getRecipientId().getCompanyName());
            }

            transactionService.createTransaction(
                    TransactionRequest.builder()
                            .recipientId(financingReceivable.getInvoice().getSenderId())
                            .amount(financing_amount)
                            .createdDate(new Date())
                            .paymentStatus(PaymentStatus.COMPLETED)
                            .financingType(FinancingType.RECEIVABLE)
                            .build()
            );

            financingReceivable.setStatus(FinancingStatus.ONGOING);
            financingReceivableRepository.saveAndFlush(financingReceivable);

            AcceptDetailResponse seller = AcceptDetailResponse.builder()
                    .company_name(financingReceivable.getInvoice().getSenderId().getCompanyName())
                    .amountPaid(financing_amount)
                    .financingType(FinancingType.RECEIVABLE.name())
                    .build();

            return AcceptResponse.builder()
                    .payment_type(partialFinancing.name())
                    .buyyer(buyyer)
                    .seller(seller)
                    .build();

        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "type invalid");
        }
    }

    @Override
    public RejectResponse backoffice_reject(RejectRequest request) {
        if (request.getType().equalsIgnoreCase("payable")) {
            FinancingPayable financingPayable = financingPayableRepository.findById(request.getFinancing_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid Financial Id"));
            financingPayable.setStatus(FinancingStatus.REJECTED);
            financingPayableRepository.saveAndFlush(financingPayable);
            return RejectResponse.builder().build();
        } else if (request.getType().equalsIgnoreCase("receivable")) {
            FinancingReceivable financingReceivable = financingReceivableRepository.findById(request.getFinancing_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid Financial Id"));
            financingReceivable.setStatus(FinancingStatus.REJECTED);
            financingReceivableRepository.saveAndFlush(financingReceivable);
            return RejectResponse.builder().build();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "type invalid");
        }
    }

    private FinancingResponse mapToResponseReceivable(FinancingReceivable data) {
        return FinancingResponse.builder()
                .financing_id(data.getFinancingId())
                .invoice_number(data.getInvoice().getInvoiceId())
                .Amount(data.getAmount())
                .company_name(data.getCompany().getCompanyName())
                .status(String.valueOf(data.getStatus()))
                .date(data.getDisbursment_date())
                .build();
    }

    private FinancingResponse mapToResponsePayable(FinancingPayable data) {
        return FinancingResponse.builder()
                .financing_id(data.getFinancingPayableId())
                .invoice_number(data.getInvoice().getInvoiceId())
                .Amount(data.getAmount())
                .company_name(data.getCompany().getCompanyName())
                .status(String.valueOf(data.getStatus()))
                .date(Date.from(data.getCreatedDate().atZone(ZoneId.systemDefault()).toInstant()))
                .build();
    }
}
