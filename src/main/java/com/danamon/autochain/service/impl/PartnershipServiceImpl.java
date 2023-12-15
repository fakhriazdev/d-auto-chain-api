package com.danamon.autochain.service.impl;

import com.danamon.autochain.constant.UserRoleType;
import com.danamon.autochain.dto.FileResponse;
import com.danamon.autochain.dto.company.*;
import com.danamon.autochain.dto.partnership.PartnershipResponse;
import com.danamon.autochain.dto.partnership.SearchPartnershipRequest;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.CompanyFile;
import com.danamon.autochain.entity.Partnership;
import com.danamon.autochain.entity.User;
import com.danamon.autochain.repository.CompanyRepository;
import com.danamon.autochain.repository.PartnershipRepository;
import com.danamon.autochain.service.CompanyFileService;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.service.PartnershipService;
import com.danamon.autochain.service.UserService;
import com.danamon.autochain.util.MailSender;
import com.danamon.autochain.util.RandomPasswordUtil;
import com.danamon.autochain.util.ValidationUtil;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnershipServiceImpl implements PartnershipService {
    private final PartnershipRepository partnershipRepository;

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
                .partnerStatus(partnership.getPartnerStatus())
                .partnerRequestedDate(partnership.getPartnerRequestedDate().toString())
                .partnerConfirmationDate(partnership.getPartnerConfirmationDate().toString())
                .requestedBy(partnership.getRequestedBy().getCompany_id())
                .confirmedBy(partnership.getConfirmedBy().getCompany_id())
                .build();
    }
}
