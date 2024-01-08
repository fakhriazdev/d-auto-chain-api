package com.danamon.autochain.service;

import com.danamon.autochain.dto.auth.UserRegisterResponse;
import com.danamon.autochain.dto.manage_user.ManageUserResponse;
import com.danamon.autochain.dto.manage_user.SearchManageUserRequest;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.Credential;
import com.danamon.autochain.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ManageUserService {
     Page<ManageUserResponse> getAllUser(SearchManageUserRequest request);
}

