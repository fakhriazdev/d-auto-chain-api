package com.danamon.autochain.service;

import com.danamon.autochain.entity.CompanyFile;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface CompanyFileService {
    CompanyFile createFile(MultipartFile multipartFile);
    Resource findByPath(String path);
    CompanyFile findById(String id);
    void deleteFile(CompanyFile companyFile);
}
