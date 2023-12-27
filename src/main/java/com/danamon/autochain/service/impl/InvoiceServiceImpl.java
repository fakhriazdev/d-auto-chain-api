package com.danamon.autochain.service.impl;


import com.danamon.autochain.dto.Invoice.InvoiceResponse;
import com.danamon.autochain.dto.Invoice.RequestInvoice;
import com.danamon.autochain.dto.Invoice.SearchInvoiceRequest;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.Credential;
import com.danamon.autochain.entity.Invoice;
import com.danamon.autochain.entity.User;
import com.danamon.autochain.repository.InvoiceRepository;
import com.danamon.autochain.repository.UserRepository;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.service.CredentialService;
import com.danamon.autochain.service.InvoiceService;
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

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final CredentialService credentialService;
    private final CompanyService companyService;
    private final UserRepository userRepository;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Invoice invoiceGeneration(RequestInvoice requestInvoice) {
        //get current user login
        Credential principal = (Credential) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        //validation user id in request invoice
        Credential userDetails = (Credential) credentialService.loadUserByUserId(requestInvoice.getRecipientId());

        //get userDetails data (include company) by user current login
        User currentUserLogin = userRepository.findUserByCredential(principal).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User Not Found"));

        //get userDetails data (include company) by recipient
        User recipientData = userRepository.findUserByCredential(userDetails.getUser().getCredential()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User Not Found"));

        //setup Invoice
        Invoice invoice = Invoice.builder()
                .senderId(currentUserLogin.getCompany())
                .recipientId(recipientData.getCompany())
                .dueDate(requestInvoice.getDueDate())
                .status(requestInvoice.getStatus())
                .amount(requestInvoice.getAmount())
                .build();

        invoiceRepository.saveAndFlush(invoice);
        return null;
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

//            if (request.getType() != null) {
//                Predicate type = criteriaBuilder.equal(
//                        criteriaBuilder.lower(root.get("type")),
//                        request.getType().toLowerCase()
//                );
//                predicates.add(type);
//            }

            String column = "senderId";
            assert request.getType() != null;

            if(request.getType().equals("payable")){
                column = "recipientId";
            }

            Predicate id = criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get(column)),
                    recipientCompany.getCompany_id()
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
                .invoice_id(invoice.getInvoiceId())
                .amount(invoice.getAmount())
                .company(invoice.getSenderId().getCompanyName())
                .status(invoice.getStatus())
                .dueDate(invoice.getDueDate())
                .build();
    }

    private InvoiceResponse mapToResponseReceivable(Invoice invoice) {
        return InvoiceResponse.builder()
                .invoice_id(invoice.getInvoiceId())
                .amount(invoice.getAmount())
                .company(invoice.getRecipientId().getCompanyName())
                .status(invoice.getStatus())
                .dueDate(invoice.getDueDate())
                .build();
    }
}
