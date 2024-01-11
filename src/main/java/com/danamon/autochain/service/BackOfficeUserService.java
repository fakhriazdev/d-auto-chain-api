package com.danamon.autochain.service;

import com.danamon.autochain.constant.RoleType;
import com.danamon.autochain.controller.backOffice.BackOfficeUserController;
import com.danamon.autochain.dto.auth.BackOfficeRegisterRequest;
import com.danamon.autochain.dto.auth.BackOfficeRegisterResponse;
import com.danamon.autochain.dto.backoffice.BackOfficeUserRequest;
import com.danamon.autochain.dto.backoffice.BackOfficeUserResponse;
import com.danamon.autochain.dto.backoffice.BackOfficeViewResponse;
import com.danamon.autochain.dto.company.SearchCompanyRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BackOfficeUserService {
    Page<BackOfficeUserResponse> getAllBackOfficeUser(BackOfficeUserRequest backOfficeUserRequest);
    BackOfficeViewResponse<?> getAccessibility(SearchCompanyRequest searchCompanyRequest);
    BackOfficeViewResponse<?> getAccessibility(List<RoleType> roleTypes);
    BackOfficeRegisterResponse addBackOfficeUser(BackOfficeRegisterRequest request);
    BackOfficeUserResponse getBackOfficeUserById(String id);
    void updateBackofficeUser(BackOfficeUserController.EditBackOfficeUser request);

}
