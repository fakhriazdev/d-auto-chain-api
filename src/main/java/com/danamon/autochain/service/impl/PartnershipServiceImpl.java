package com.danamon.autochain.service.impl;

import com.danamon.autochain.constant.PartnershipStatus;
import com.danamon.autochain.dto.partnership.NewPartnershipRequest;
import com.danamon.autochain.dto.partnership.PartnershipResponse;
import com.danamon.autochain.dto.partnership.SearchPartnershipRequest;
import com.danamon.autochain.entity.*;
import com.danamon.autochain.repository.PartnershipRepository;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.service.PartnershipService;
import com.danamon.autochain.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnershipServiceImpl implements PartnershipService {
    private final PartnershipRepository partnershipRepository;
    private final ValidationUtil validationUtil;
    private final CompanyService companyService;

    @Override
    public String rejectPartnership(String partnershipId) {
        Partnership partnership = findByIdOrThrowNotFound(partnershipId);

        partnershipRepository.delete(partnership);

        return "Rejected partnership";
    }

    @Override
    public PartnershipResponse acceptPartnership(String partnershipId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Credential userCredential = (Credential) authentication.getPrincipal();

        Partnership partnership = findByIdOrThrowNotFound(partnershipId);
        partnership.setPartnerStatus(PartnershipStatus.IN_PARTNER);
        partnership.setPartnerConfirmationDate(LocalDateTime.now());
        partnership.setConfirmedBy(userCredential);
        Partnership partnershipUpdated = partnershipRepository.saveAndFlush(partnership);

        return mapToResponse(partnershipUpdated);
    }

    private Partnership findByIdOrThrowNotFound(String id) {
        return partnershipRepository.findById(id).orElseThrow(() -> new RuntimeException("partnerships not found"));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public PartnershipResponse addPartnership(NewPartnershipRequest request) {
        validationUtil.validate(request);

        Company company = companyService.getById(request.getCompanyId());
        Company partner = companyService.getById(request.getPartnerId());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Credential userCredential = (Credential) authentication.getPrincipal();

        Partnership partnership = Partnership.builder()
                .company(company)
                .partner(partner)
                .partnerStatus(PartnershipStatus.PENDING)
                .partnerRequestedDate(LocalDateTime.now())
                .partnerConfirmationDate(null)
                .requestedBy(userCredential)
                .confirmedBy(null)
                .build();

        Partnership partnershipSaved = partnershipRepository.saveAndFlush(partnership);

        return mapToResponse(partnershipSaved);
    }
    @Transactional(readOnly = true)
    @Override
    public Page<PartnershipResponse> getAll(String id, SearchPartnershipRequest request) {
        Sort.Direction direction = Sort.Direction.fromString(request.getDirection());
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), direction, request.getSortBy());
        Page<Partnership> parterships = partnershipRepository.findAllByCompanyId(id, pageable);
        return parterships.map(this::mapToResponse);
    }

    private PartnershipResponse mapToResponse(Partnership partnership) {
        return PartnershipResponse.builder()
                .partnershipId(partnership.getPartnership_no())
                .companyId(partnership.getCompany().getCompany_id())
                .partnerId(partnership.getPartner().getCompany_id())
                .partnerStatus(partnership.getPartnerStatus().toString())
                .partnerRequestedDate(partnership.getPartnerRequestedDate().toString())
                .partnerConfirmationDate(partnership.getPartnerConfirmationDate() != null ? partnership.getPartnerConfirmationDate().toString() : null)
                .requestedBy(partnership.getRequestedBy().getCredentialId())
                .confirmedBy(partnership.getConfirmedBy() != null ? partnership.getConfirmedBy().getCredentialId() : null)
                .build();
    }
}
