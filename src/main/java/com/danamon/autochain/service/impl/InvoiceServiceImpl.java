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
    private final CompanyService companyService;
    private final ObjectMapper objectMapper;

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
                .invDate(requestInvoice.getInvDate())
                .amount(requestInvoice.getAmount())
                .createdDate(LocalDateTime.now())
                .createdBy(principal.getCredentialId())
                .itemList(requestInvoice.getItemList())
                .build();

        invoiceRepository.saveAndFlush(invoice);

        List<ItemList> itemLists = mapStringToJson(invoice.getItemList());
        return InvoiceResponse.builder()
                .companyName(invoice.getRecipientId().getCompanyName())
                .Status(String.valueOf(invoice.getStatus()))
                .invNumber(invoice.getInvoiceId())
                .dueDate(invoice.getDueDate())
                .amount(invoice.getAmount())
                .itemList(itemLists)
                .build();
    }

    @Override
    public InvoiceDetailResponse getInvoiceDetail(String id) {
        // get invoice by id
        Invoice invoice = invoiceRepository.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.CONFLICT, "Data Not Found"));

        // map json to string
        return mapToInvoiceDetailResponse(invoice);
    }

    @Override
    public InvoiceDetailResponse updateInvoiceStatus(String id, ProcessingStatusType processingStatusType) {

        Invoice invoice = invoiceRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Data Not Found"));

        if (invoice.getProcessingStatus()!= null){
            if (invoice.getProcessingStatus().equals(ProcessingStatusType.CANCEL_INVOICE)){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice With Status CANCEL Cannot Be Updated");
            }
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
                .date(invoice.getInvDate())
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
