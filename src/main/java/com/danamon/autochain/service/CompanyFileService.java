package com.danamon.autochain.service;

import com.danamon.autochain.entity.CompanyFile;
import org.springframework.web.multipart.MultipartFile;

public interface CompanyFileService {
    CompanyFile createFile(MultipartFile multipartFile);
}
