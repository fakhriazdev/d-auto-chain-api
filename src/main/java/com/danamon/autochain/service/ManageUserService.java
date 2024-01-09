package com.danamon.autochain.service;

import com.danamon.autochain.dto.auth.UserRegisterResponse;
import com.danamon.autochain.dto.manage_user.ManageUserResponse;
import com.danamon.autochain.dto.manage_user.NewUserRequest;
import com.danamon.autochain.dto.manage_user.SearchManageUserRequest;
import com.danamon.autochain.dto.manage_user.UpdateUserRequest;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.Credential;
import com.danamon.autochain.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ManageUserService {
     Page<ManageUserResponse> getAllUser(SearchManageUserRequest request);
     ManageUserResponse createUser(NewUserRequest request);
     ManageUserResponse updateUser(UpdateUserRequest request);
     User getById(String id);
}

