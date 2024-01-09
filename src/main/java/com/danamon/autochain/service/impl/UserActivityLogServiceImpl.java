package com.danamon.autochain.service.impl;

import com.danamon.autochain.constant.ActorType;
import com.danamon.autochain.constant.RoleType;
import com.danamon.autochain.dto.FileResponse;
import com.danamon.autochain.dto.company.CompanyResponse;
import com.danamon.autochain.dto.company.NewCompanyRequest;
import com.danamon.autochain.dto.company.SearchCompanyRequest;
import com.danamon.autochain.dto.company.UpdateCompanyRequest;
import com.danamon.autochain.entity.*;
import com.danamon.autochain.repository.*;
import com.danamon.autochain.security.BCryptUtil;
import com.danamon.autochain.service.CompanyFileService;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.service.UserActivityLogService;
import com.danamon.autochain.util.MailSender;
import com.danamon.autochain.util.RandomPasswordUtil;
import com.danamon.autochain.util.ValidationUtil;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivityLogServiceImpl implements UserActivityLogService {
    private final UserActivityLogRepository userActivityLogRepository;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserActivityLog create(UserActivityLog userActivityLog) {
        try {
            return userActivityLogRepository.save(userActivityLog);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
