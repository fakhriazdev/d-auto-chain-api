package com.danamon.autochain.service.impl;

import com.danamon.autochain.constant.RoleType;
import com.danamon.autochain.constant.TenureStatus;
import com.danamon.autochain.constant.financing.FinancingStatus;
import com.danamon.autochain.constant.financing.FinancingType;
import com.danamon.autochain.constant.invoice.ProcessingStatusType;
import com.danamon.autochain.constant.payment.PaymentMethod;
import com.danamon.autochain.constant.payment.PaymentStatus;
import com.danamon.autochain.constant.payment.PaymentType;
import com.danamon.autochain.controller.dashboard.BackOfficeDashboardController;
import com.danamon.autochain.dto.backoffice_dashboard.PerformanceCompanyResponse;
import com.danamon.autochain.dto.financing.*;
import com.danamon.autochain.entity.*;
import com.danamon.autochain.repository.*;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.service.FinancingService;
import com.danamon.autochain.service.PaymentService;
import com.danamon.autochain.util.IdsGeneratorUtil;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
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
import java.util.concurrent.atomic.AtomicReference;

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
            Payment payment = paymentRepository.findById(request.getPayment_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid payment id : " + request.getPayment_id()));

            if (payment.getAmount() < 75000000)
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Amount cannot low than Rp.75.000.000 with ID payment : " + request.getPayment_id());

            double interest = 0.01d;

            long loanAmount = payment.getAmount();

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
            String id = IdsGeneratorUtil.generate("FIN", company.getCompany_id());

            financingPayableRepository.saveAndFlush(
                    FinancingPayable.builder()
                            .financingPayableId(id)
                            .company(company)
                            .invoice(payment.getInvoice())
                            .payment(payment)
                            .createdBy(user.getName())
                            .createdDate(LocalDateTime.now())
                            .amount(payment.getAmount())
                            .monthly_installment(paymentPermount)
                            .interest(interest)
                            .total(loanAmount + (loanAmount * interest))
                            .period_number(0)
                            .tenure(request.getTenure())
                            .status(FinancingStatus.PENDING)
                            .paymentMethod(PaymentMethod.valueOf(request.getPayment_method()))
                            .build()
            );
        });
    }

    @Override
    public Page<FinancingResponse> get_all_payable(SearchFinancingRequest request) {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findUserByCredential(principal).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credential invalid"));
        Company company = companyRepository.findById(user.getCompany().getCompany_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "invalid company id"));

        List<Company> senders = new ArrayList<>(user.getUserAccsess().stream().map(UserAccsess::getCompany).toList());

        List<Invoice> invoices = invoiceRepository.findAllBySenderIdInAndRecipientId(senders, company);

        boolean isSuperUser = principal.getRoles().stream()
                .anyMatch(role -> role.getRole().getRoleName().equals(RoleType.SUPER_USER.getName()));

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

            if (!isSuperUser) {
                predicates.add(root.get("invoice").in(invoices));
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
                    .due_date(tenure.getDueDate().toString())
                    .amount(tenure.getAmount())
                    .status(tenure.getStatus().name())
                    .build());
        });
        return PayableDetailResponse.builder()
                .financing_id(financingPayable.getFinancingPayableId())
                .payment_id(financingPayable.getPayment().getPaymentId())
                .recipient(recipient)
                .sender(sender)
                .total_amount(financingPayable.getAmount())
                .created_date(financingPayable.getCreatedDate().toString())
                .tenure(financingPayable.getTenure())
                .amount_instalment(financingPayable.getMonthly_installment())
                .tenure_list_detail(listTenure)
                .status(financingPayable.getStatus().name())
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

            if (receivableRequest.getAmount() < 75000000) {
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Amount cannot less than Rp.75.000.000");
            }

            if(company.getRemainingLimit() < receivableRequest.getAmount()){
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Amount cannot less than remaining limit company : "+ company.getRemainingLimit());
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
                            .financingId(IdsGeneratorUtil.generate("FIN",company.getCompany_id()))
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

        List<Company> recipients = new ArrayList<>(user.getUserAccsess().stream().map(UserAccsess::getCompany).toList());

        List<Invoice> invoices = invoiceRepository.findAllByRecipientIdInAndSenderId(recipients, company);

        boolean isSuperUser = principal.getRoles().stream()
                .anyMatch(role -> role.getRole().getRoleName().equals(RoleType.SUPER_USER.getName()));

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

            if (!isSuperUser) {
                predicates.add(root.get("invoice").in(invoices));
            }

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
                .type(financingReceivable.getFinancingType().name())
                .created_date(financingReceivable.getDisbursment_date())
                .status(financingReceivable.getStatus().toString())
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
            return payableFinancing(request);
        } else if (request.getType().equalsIgnoreCase("receivable")) {
            return receivableFinancing(request);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "type invalid");
        }
    }

    @Override
    public void backoffice_reject(RejectRequest request) {
        if (request.getType().equalsIgnoreCase("payable")) {
            FinancingPayable financingPayable = financingPayableRepository.findById(request.getFinancing_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid Financial Id"));
            financingPayable.setStatus(FinancingStatus.REJECTED);
            financingPayableRepository.saveAndFlush(financingPayable);

        } else if (request.getType().equalsIgnoreCase("receivable")) {
            FinancingReceivable financingReceivable = financingReceivableRepository.findById(request.getFinancing_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid Financial Id"));
            financingReceivable.setStatus(FinancingStatus.REJECTED);
            financingReceivableRepository.saveAndFlush(financingReceivable);

        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "type invalid");
        }
    }

    @Override
    public List<PerformanceCompanyResponse> get_performance(String filter) {
        List<PerformanceCompanyResponse> data = new ArrayList<>();

//        =================== TOTAL =============

        if(filter.equalsIgnoreCase("total")){
            List<Object[]> fpr = financingPayableRepository.findAllAndSumByAmountGroupByCompanyName();
            List<Object[]> fpc = financingReceivableRepository.findAllAndSumByAmountGroupByCompanyName();

            for (Object[] row : fpr) {
                data.add(PerformanceCompanyResponse.builder()
                        .company_name((String) row[0])
                        .value((Double)row[1])
                        .build()
                );
            }

            for (Object[] row : fpc) {
                data.add(PerformanceCompanyResponse.builder()
                        .company_name((String) row[0])
                        .value((Double)row[1])
                        .build()
                );
            }

            Collections.sort(data, Comparator.comparingDouble(PerformanceCompanyResponse::getValue).reversed());
            return data;

//            ================= AVERAGE =============

        } else if (filter.equalsIgnoreCase("average")) {
            List<Object[]> fpr = financingPayableRepository.findAllByAverageSumGroupByCompanyName();
            List<Object[]> fpc = financingReceivableRepository.findAllByAverageSumGroupByCompanyName();

            for (Object[] row : fpr) {
                data.add(PerformanceCompanyResponse.builder()
                        .company_name((String) row[0])
                        .value((Double)row[1])
                        .build()
                );
            }

            for (Object[] row : fpc) {
                data.add(PerformanceCompanyResponse.builder()
                        .company_name((String) row[0])
                        .value((Double)row[1])
                        .build()
                );
            }

            Collections.sort(data, Comparator.comparingDouble(PerformanceCompanyResponse::getValue).reversed());
            return data;

//            ============= APPROVED COUNT =============

        } else if (filter.equalsIgnoreCase("approved")) {
            List<Object[]> fpr = financingPayableRepository.findAllByStatusGroupByCompanyName("ONGOING");
            List<Object[]> fpc = financingReceivableRepository.findAllByStatusGroupByCompanyName("ONGOING");

            for (Object[] row : fpr) {
                data.add(PerformanceCompanyResponse.builder()
                        .company_name((String) row[0])
                        .value((Double)row[1])
                        .build()
                );
            }

            for (Object[] row : fpc) {
                data.add(PerformanceCompanyResponse.builder()
                        .company_name((String) row[0])
                        .value((Double)row[1])
                        .build()
                );
            }
            Collections.sort(data, Comparator.comparingDouble(PerformanceCompanyResponse::getValue).reversed());
            return data;
        }else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "filter available(TOTAL, AVERAGE, APPROVED)");
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

    @Override
    public BackOfficeDashboardController.FinancingStatResponse getAllFinanceStat() {
        // payable
        Long ongoingPayable = financingPayableRepository.countByStatus(FinancingStatus.ONGOING);
        Long outstandingPayable = financingPayableRepository.countByStatus(FinancingStatus.OUTSTANDING);
        Long pendingPayable = financingPayableRepository.countByStatus(FinancingStatus.PENDING);

        // receivable
        Long ongoingReceivable = financingReceivableRepository.countByStatus(FinancingStatus.ONGOING);
        Long outstandingReceivable = financingReceivableRepository.countByStatus(FinancingStatus.OUTSTANDING);
        Long pendingReceivable = financingReceivableRepository.countByStatus(FinancingStatus.PENDING);

        return new BackOfficeDashboardController.FinancingStatResponse(ongoingPayable + ongoingReceivable, outstandingPayable + outstandingReceivable, pendingReceivable + pendingPayable);
    }

    @Override
    public List<BackofficeFinanceResponse> backoffice_get_all_financing() {
        List<BackofficeFinanceResponse> result = new ArrayList<>();
        Page<FinancingPayable> fpr = financingPayableRepository.findAll(PageRequest.of(1, 5, Sort.Direction.ASC, "createdDate"));
        Page<FinancingReceivable> fpc = financingReceivableRepository.findAll(PageRequest.of(1, 5, Sort.Direction.ASC, "createdDate"));

        fpr.stream().map(financingPayable ->
                result.add(new BackofficeFinanceResponse(financingPayable.getCreatedDate(), financingPayable.getCreatedBy(), financingPayable.getStatus()))
        );
        fpc.stream().map(financingReceivable ->
                result.add(new BackofficeFinanceResponse(financingReceivable.getCreatedDate(), financingReceivable.getCreatedBy(), financingReceivable.getStatus()))
        );

        return result.stream().sorted(Comparator.comparing(BackofficeFinanceResponse::financeDate)).toList();
    }

    @Override
    public Long getTotalPaidFinancingPayable() {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<FinancingPayable> financingPayables = financingPayableRepository.findAllByCompanyAndStatusIs(principal.getUser().getCompany(), FinancingStatus.COMPLETED);

        long total = 0;

        for (FinancingPayable financingPayable : financingPayables) {
            total += financingPayable.getAmount();
        }

        return total;
    }

    @Override
    public Long getTotalUnpaidFinancingPayable() {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<FinancingPayable> financingPayables = financingPayableRepository.findAllByCompanyAndStatusIsOrStatusIs(principal.getUser().getCompany(), FinancingStatus.ONGOING, FinancingStatus.OUTSTANDING);

        long total = 0;

        for (FinancingPayable financingPayable : financingPayables) {
            total += financingPayable.getAmount();
        }

        return total;
    }

    @Override
    public Long getTotalFinancingEarlyDisbursement() {
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return financingReceivableRepository.findAllByCompanyAndStatusIsAndStatusIs(principal.getUser().getCompany(), FinancingStatus.ONGOING, FinancingStatus.COMPLETED).orElse(0L);
    }

    public record BackofficeFinanceResponse(LocalDateTime financeDate, String issuer, FinancingStatus status) {
    }

    public AcceptResponse payableFinancing(AcceptRequest request){
        FinancingPayable financingPayable = financingPayableRepository.findById(request.getFinancing_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid Financial Id"));
        if(financingPayable.getInvoice().getProcessingStatus().equals(ProcessingStatusType.WAITING_STATUS)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"this id invoice not yet approved by company recipient");
        if(financingPayable.getInvoice().getProcessingStatus().equals(ProcessingStatusType.REJECT_INVOICE)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"this id invoice already rejected by company recipient");
        if(financingPayable.getInvoice().getProcessingStatus().equals(ProcessingStatusType.CANCEL_INVOICE)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"this id invoice already canceled by company sender");
        if(financingPayable.getStatus().equals(FinancingStatus.ONGOING)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"this financing id already approved by backoffice");
        if(financingPayable.getPayment().getType().equals(PaymentType.FINANCING_PAYABLE)) throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,"Cannot Request With Type Financing Payable With same Payment ID : "+financingPayable.getPayment().getPaymentId());
//        Double tenure_amount = financingPayable.getTotal() / financingPayable.getTenure();

        for (int i = 1; i <= financingPayable.getTenure(); i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(financingPayable.getInvoice().getDueDate());
            calendar.add(Calendar.MONTH, i);
            Date dueDate = calendar.getTime();

            // Set the tenure status
            TenureStatus tenureStatus = (i > 1) ? TenureStatus.UPCOMING : TenureStatus.UNPAID;

            tenureRepository.saveAndFlush(
                    Tenure.builder()
                            .financingPayableId(financingPayable)
                            .dueDate(dueDate)
                            .status(tenureStatus)
                            .Amount(financingPayable.getMonthly_installment())
                            .build()
            );
        }

        Payment payment = financingPayable.getPayment();

        if(payment.getType().equals(PaymentType.INVOICING)){


            Payment DanamonToSeller = Payment.builder()
                    .paymentId(IdsGeneratorUtil.generate("PAY", "DANAMON"))
                    .recipientId(null)
                    .senderId(payment.getInvoice().getSenderId())
                    .invoice(payment.getInvoice())
                    .type(PaymentType.FINANCING_PAYABLE)
                    .status(PaymentStatus.PAID)
                    .method(PaymentMethod.AUTO_DEBIT)
                    .amount(payment.getAmount())
                    .createdDate(new Date())
                    .dueDate(payment.getDueDate())
                    .paidDate(new Date())
                    .build();

            payment.setAmount(financingPayable.getTotal().longValue());
            payment.setType(PaymentType.FINANCING_PAYABLE);
            payment.setSenderId(null);
            payment.setDueDate(new Date());

            paymentRepository.saveAndFlush(payment);
            paymentRepository.saveAndFlush(DanamonToSeller);

        } else if (payment.getType().equals(PaymentType.PARTIAL_FINANCING) || payment.getType().equals(PaymentType.FINANCING_RECEIVABLE)) {
            payment.setAmount(financingPayable.getTotal().longValue());
            payment.setType(PaymentType.FINANCING_PAYABLE);
            payment.setSenderId(null);
            payment.setDueDate(new Date());
            paymentRepository.saveAndFlush(payment);
        }

        financingPayable.setStatus(FinancingStatus.ONGOING);
        financingPayableRepository.saveAndFlush(financingPayable);

        Company company = companyRepository.findById(financingPayable.getCompany().getCompany_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid Company ID for decrease remaining limit"));
        double new_remaining_limit = company.getRemainingLimit() - financingPayable.getAmount();
        if(new_remaining_limit < 0) throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Remaining Limit Less than 0, cannot request financing");
        company.setRemainingLimit(new_remaining_limit);
        companyRepository.saveAndFlush(company);

//        AcceptDetailResponse buyyer = new AcceptDetailResponse();
//        buyyer.setFinancingType(FinancingType.PAYABLE.name());
//        buyyer.setAmountPaid(financingPayable.getTotal().longValue());
//        buyyer.setCompany_name(financingPayable.getInvoice().getRecipientId().getCompanyName());
//
//        AcceptDetailResponse seller = AcceptDetailResponse.builder()
//                .company_name(financingPayable.getInvoice().getSenderId().getCompanyName())
//                .amountPaid(financingPayable.getAmount())
//                .financingType(FinancingType.RECEIVABLE.name())
//                .build();

        return AcceptResponse.builder()
//                .payment_type(FinancingType.PAYABLE.name())
//                .buyyer(buyyer)
//                .seller(seller)
//                .invoice_amount(financingPayable.getInvoice().getAmount().doubleValue())
                .build();
    }

    public AcceptResponse receivableFinancing(AcceptRequest request){
        FinancingReceivable financingReceivable = financingReceivableRepository.findById(request.getFinancing_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid Financial Id"));
        if(financingReceivable.getInvoice().getProcessingStatus().equals(ProcessingStatusType.WAITING_STATUS)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"this id invoice not yet approved by company recipient");
        if(financingReceivable.getInvoice().getProcessingStatus().equals(ProcessingStatusType.REJECT_INVOICE)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"this id invoice already rejected by company recipient");
        if(financingReceivable.getInvoice().getProcessingStatus().equals(ProcessingStatusType.CANCEL_INVOICE)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"this id invoice already canceled by company sender");
        if(financingReceivable.getStatus().equals(FinancingStatus.ONGOING)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"this financing id already approved by backoffice");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, 1);
        Date dueDate = calendar.getTime();

        List<Payment> payments = financingReceivable.getInvoice().getPayment();
        Invoice invoice_financing = financingReceivable.getInvoice();

//        Payment payment = paymentRepository.findById(request.getPayment_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid payment ID with : " + request.getPayment_id()));

        Long financing_amount = financingReceivable.getAmount();
        Long invoice_amount = invoice_financing.getAmount();
        Long payable_amount = invoice_amount - financing_amount;

        if (payable_amount < 0 || financing_amount > invoice_amount) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid financing request amount with amount : "+financing_amount);
        }

        AtomicReference<PaymentType> partialFinancing = new AtomicReference<>(PaymentType.FINANCING_RECEIVABLE);
        AcceptDetailResponse buyyer = new AcceptDetailResponse();
        buyyer.setFinancingType(FinancingType.PAYABLE.name());
        buyyer.setAmountPaid(payable_amount);
        buyyer.setCompany_name(financingReceivable.getInvoice().getRecipientId().getCompanyName());

        List<Payment> updatedPayment = new ArrayList<>();

        payments.forEach(payment -> {

//            ===================== FULL FINANCING ================
            if(payable_amount.equals(0L) && payment.getType().equals(PaymentType.INVOICING)){
                if(payment.getInvoice().getSenderId().equals(invoice_financing.getSenderId())){

                    // ==========  UPDATE BUYER ke DANAMON ===========
                    payment.setAmount(invoice_amount);
                    payment.setSenderId(null);
                    payment.setType(partialFinancing.get());
                    updatedPayment.add(payment);

                    // ============= CREATE DANAMON to SELLER ===========
                    Payment buyerToDanamon = Payment.builder()
                            .paymentId(IdsGeneratorUtil.generate("PAY", invoice_financing.getSenderId().getCompany_id()))
                            .recipientId(null)
                            .senderId(financingReceivable.getInvoice().getSenderId())
                            .invoice(financingReceivable.getInvoice())
                            .type(PaymentType.FINANCING_RECEIVABLE)
                            .status(PaymentStatus.PAID)
                            .method(PaymentMethod.AUTO_DEBIT)
                            .amount(financingReceivable.getTotal().longValue())
                            .createdDate(new Date())
                            .dueDate(dueDate)
                            .paidDate(new Date())
                            .build();

                    updatedPayment.add(buyerToDanamon);
                }
            }

            //            ========================== PARTIAL DANAMON ==========================
//            buat partial payment ke danamon & seller
            if (financing_amount < invoice_amount) {
                partialFinancing.set(PaymentType.INVOICING);
                try{
                    if(payment.getInvoice().getSenderId().equals(invoice_financing.getSenderId()) && payment.getType().equals(PaymentType.INVOICING)){
                        // ==========  BUYER ke SELLER ===========
                        payment.setAmount(payable_amount);
                        payment.setType(partialFinancing.get());
                        updatedPayment.add(payment);
//                        paymentRepository.saveAndFlush(payment);


                        // ============= BUYER ke DANAMON ===========
                        Payment buyerToDanamon = Payment.builder()
                                .paymentId(IdsGeneratorUtil.generate("PAY", "DANAMON"))
                                .recipientId(financingReceivable.getInvoice().getRecipientId())
                                .senderId(null)
                                .invoice(financingReceivable.getInvoice())
                                .type(PaymentType.PARTIAL_FINANCING)
                                .status(PaymentStatus.UNPAID)
                                .method(payment.getMethod())
                                .amount(financing_amount)
                                .createdDate(new Date())
                                .dueDate(dueDate)
                                .paidDate(null)
                                .build();

                        updatedPayment.add(buyerToDanamon);
//                        paymentRepository.saveAndFlush(toDanamon);

                        // ============= DANAMON to SELLER ===========
                        Payment DanamonToSeller = Payment.builder()
                                .paymentId(IdsGeneratorUtil.generate("PAY", invoice_financing.getSenderId().getCompany_id()))
                                .recipientId(null)
                                .senderId(financingReceivable.getInvoice().getSenderId())
                                .invoice(financingReceivable.getInvoice())
                                .type(PaymentType.PARTIAL_FINANCING)
                                .status(PaymentStatus.PAID)
                                .method(PaymentMethod.AUTO_DEBIT)
                                .amount(financingReceivable.getTotal().longValue())
                                .createdDate(new Date())
                                .dueDate(dueDate)
                                .paidDate(new Date())
                                .build();

                        updatedPayment.add(DanamonToSeller);
                    }
                } catch (Exception e){
                    System.out.println(e+" \ninvalid company ID : "+ payment.getInvoice().getSenderId());
                }
            }
        });

        paymentRepository.saveAllAndFlush(updatedPayment);

        financingReceivable.setStatus(FinancingStatus.ONGOING);
        financingReceivableRepository.saveAndFlush(financingReceivable);

        Company company = companyRepository.findById(financingReceivable.getCompany().getCompany_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid Company ID for decrease remaining limit"));
        double new_remaining_limit = company.getRemainingLimit() - financing_amount;
        if(new_remaining_limit < 0) throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Remaining Limit Less than 0, cannot request financing");
        company.setRemainingLimit(new_remaining_limit);
        companyRepository.saveAndFlush(company);

//        Response DTO
        buyyer.setFinancingType(FinancingType.PAYABLE.name());
        buyyer.setAmountPaid(payable_amount);
        buyyer.setCompany_name(financingReceivable.getInvoice().getRecipientId().getCompanyName());

        AcceptDetailResponse seller = AcceptDetailResponse.builder()
                .company_name(financingReceivable.getInvoice().getSenderId().getCompanyName())
                .amountPaid(financing_amount)
                .financingType(FinancingType.RECEIVABLE.name())
                .build();

        return AcceptResponse.builder()
                .payment_type(partialFinancing.get().name())
                .invoice_amount(invoice_amount.doubleValue())
                .buyyer(buyyer)
                .seller(seller)
                .build();
    }
}
