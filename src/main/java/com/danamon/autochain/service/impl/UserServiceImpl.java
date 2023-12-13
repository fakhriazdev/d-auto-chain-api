package com.danamon.autochain.service.impl;

import com.danamon.autochain.dto.auth.UserRegisterRequest;
import com.danamon.autochain.dto.auth.UserRegisterResponse;
import com.danamon.autochain.dto.user.UserRequest;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.Credential;
import com.danamon.autochain.entity.User;
import com.danamon.autochain.repository.CompanyRepository;
import com.danamon.autochain.repository.CredentialRepository;
import com.danamon.autochain.repository.UserRepository;
import com.danamon.autochain.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserRegisterResponse createNew(Credential credential, Company company){
        try {
            log.info("Start createNew");
            User user = User.builder()
                    .company_id(company)
                    .credential_id(credential)
                    .build();
            userRepository.saveAndFlush(user);
            log.info("End createNew");
            return UserRegisterResponse.builder()
                    .username(credential.getUsername())
                    .email(credential.getEmail())
                    .roleType(credential.getRole().getName())
                    .build();
        } catch (DataIntegrityViolationException e) {
            log.error("Error createNew: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "user already exist");
        }
    }
}

