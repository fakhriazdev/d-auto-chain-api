package com.danamon.autochain.service;

import com.danamon.autochain.dto.auth.UserRegisterResponse;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.Credential;
public interface UserService {
     UserRegisterResponse createNew(Credential credential, Company company);
}

