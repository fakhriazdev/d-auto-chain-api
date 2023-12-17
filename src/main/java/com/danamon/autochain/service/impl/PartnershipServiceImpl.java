package com.danamon.autochain.service.impl;

import com.danamon.autochain.constant.PartnershipStatus;
import com.danamon.autochain.constant.UserRoleType;
import com.danamon.autochain.dto.company.CompanyResponse;
import com.danamon.autochain.dto.company.NewCompanyRequest;
import com.danamon.autochain.dto.company.NewCompanyResponse;
import com.danamon.autochain.dto.partnership.NewPartnershipRequest;
import com.danamon.autochain.dto.partnership.PartnershipResponse;
import com.danamon.autochain.dto.partnership.SearchPartnershipRequest;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.CompanyFile;
import com.danamon.autochain.entity.Partnership;
import com.danamon.autochain.entity.User;
import com.danamon.autochain.repository.PartnershipRepository;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.service.PartnershipService;
import com.danamon.autochain.util.MailSender;
import com.danamon.autochain.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnershipServiceImpl implements PartnershipService {
    private final PartnershipRepository partnershipRepository;
    private final ValidationUtil validationUtil;
    private final CompanyService companyService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public PartnershipResponse addPartnership(NewPartnershipRequest request) {
        validationUtil.validate(request);

        Company company = companyService.getById(request.getCompanyId());
        Company partner = companyService.getById(request.getPartnershipId());

        Partnership partnership = Partnership.builder()
                .company(company)
                .partner(partner)
                .partnerStatus(PartnershipStatus.PENDING)
                .partnerRequestedDate(LocalDateTime.now())
                .partnerConfirmationDate(null)
                .requestedBy(company)
                .confirmedBy(null)
                .build();

        Partnership partnershipSaved = partnershipRepository.saveAndFlush(partnership);

        return mapToResponse(partnershipSaved);
    }
    @Transactional(readOnly = true)
    @Override
    public Page<PartnershipResponse> getAll(SearchPartnershipRequest request) {
        Sort.Direction direction = Sort.Direction.fromString(request.getDirection());
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), direction, request.getSortBy());
        Page<Partnership> parterships = partnershipRepository.findAll(pageable);
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
                .requestedBy(partnership.getRequestedBy().getCompany_id())
                .confirmedBy(partnership.getConfirmedBy() != null ? partnership.getConfirmedBy().getCompany_id() : null)
                .build();
    }
}
