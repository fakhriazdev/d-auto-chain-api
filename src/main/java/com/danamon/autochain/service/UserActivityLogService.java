package com.danamon.autochain.service;

import com.danamon.autochain.dto.company.CompanyResponse;
import com.danamon.autochain.dto.company.NewCompanyRequest;
import com.danamon.autochain.dto.company.SearchCompanyRequest;
import com.danamon.autochain.dto.company.UpdateCompanyRequest;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.UserActivityLog;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserActivityLogService {
    UserActivityLog create(UserActivityLog userActivityLog);
}
