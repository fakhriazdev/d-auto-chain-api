package com.danamon.autochain.controller;

import com.danamon.autochain.dto.DataResponse;
import com.danamon.autochain.dto.PagingResponse;
import com.danamon.autochain.dto.company.*;
import com.danamon.autochain.entity.CompanyFile;
import com.danamon.autochain.service.CompanyFileService;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.util.PagingUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
//@SecurityRequirement(name = "Bearer Authentication")
public class CompanyController {
    private final CompanyService companyService;
    private final CompanyFileService companyFileService;

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateCompany(
            @RequestParam String id,
            @RequestParam String companyName,
            @RequestParam String province,
            @RequestParam String city,
            @RequestParam String address,
            @RequestParam String phoneNumber,
            @RequestParam String companyEmail,
            @RequestParam String accountNumber,
            @RequestParam(required = false) List<MultipartFile> files,
            @RequestParam String emailUSer,
            @RequestParam Boolean isGeneratePassword
    ) {
        UpdateCompanyRequest request = UpdateCompanyRequest.builder()
                .id(id)
                .companyName(companyName)
                .province(province)
                .city(city)
                .address(address)
                .phoneNumber(phoneNumber)
                .companyEmail(companyEmail)
                .multipartFiles(files)
                .accountNumber(accountNumber)
                .emailUser(emailUSer)
                .isGeneratePassword(isGeneratePassword)
                .build();

        CompanyResponse menuResponse = companyService.update(request);
        DataResponse<CompanyResponse> response = DataResponse.<CompanyResponse>builder()
                .message("successfully update company")
                .statusCode(HttpStatus.OK.value())
                .data(menuResponse)
                .build();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createCompany(
            @RequestParam String companyName,
            @RequestParam String province,
            @RequestParam String city,
            @RequestParam String address,
            @RequestParam String phoneNumber,
            @RequestParam String companyEmail,
            @RequestParam String accountNumber,
            @RequestParam Double financingLimit,
            @RequestParam Double remainingLimit,
            @RequestParam List<MultipartFile> files,
            @RequestParam String username,
            @RequestParam String emailUser
    ) {
        NewCompanyRequest request = NewCompanyRequest.builder()
                .companyName(companyName)
                .province(province)
                .city(city)
                .address(address)
                .phoneNumber(phoneNumber)
                .companyEmail(companyEmail)
                .accountNumber(accountNumber)
                .financingLimit(financingLimit)
                .remainingLimit(remainingLimit)
                .multipartFiles(files)
                .username(username)
                .emailUser(emailUser)
                .build();

        NewCompanyResponse companyResponse = companyService.create(request);
        DataResponse<NewCompanyResponse> response = DataResponse.<NewCompanyResponse>builder()
                .message("successfully create new company")
                .statusCode(HttpStatus.CREATED.value())
                .data(companyResponse)
                .build();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @GetMapping
    public ResponseEntity<?> getAllCompanies(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "asc") String direction,
            @RequestParam(required = false, defaultValue = "companyName") String sortBy,
            @RequestParam(required = false) String name
    ) {
        page = PagingUtil.validatePage(page);
        size = PagingUtil.validateSize(size);
        direction = PagingUtil.validateDirection(direction);

        SearchCompanyRequest request = SearchCompanyRequest.builder()
                .page(page)
                .size(size)
                .direction(direction)
                .sortBy(sortBy)
                .name(name)
                .build();
        Page<CompanyResponse> companyResponse = companyService.getAll(request);
        PagingResponse pagingResponse = PagingResponse.builder()
                .count(companyResponse.getTotalElements())
                .totalPages(companyResponse.getTotalPages())
                .page(page)
                .size(size)
                .build();
        DataResponse<List<CompanyResponse>> response = DataResponse.<List<CompanyResponse>>builder()
                .message("successfully get all company")
                .statusCode(HttpStatus.OK.value())
                .data(companyResponse.getContent())
                .paging(pagingResponse)
                .build();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<?> downloadCompanyFile(@PathVariable String id) {
        Resource resource = companyService.getCompanyFilesByIdFile(id);
        String headerValues = "attachment; filename=\"" + resource.getFilename() + "\"";
        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValues)
                .body(resource);
    }

    @DeleteMapping("/{id}/file")
    public void deleteCompanyFile(@PathVariable String id) {
        CompanyFile file = companyFileService.findById(id);
        companyFileService.deleteFile(file);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCompanyById(@PathVariable String id) {
        CompanyResponse productResponse = companyService.findById(id);
        DataResponse<CompanyResponse> response = DataResponse.<CompanyResponse>builder()
                .message("successfully get company")
                .statusCode(HttpStatus.OK.value())
                .data(productResponse)
                .build();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}/non-partnerships")
    public ResponseEntity<?> getNonPartnershipByCompanyId(@PathVariable String id) {
        List<CompanyResponse> nonPartnerships = companyService.getNonPartnership(id);
        DataResponse<List<CompanyResponse>> response = DataResponse.<List<CompanyResponse>>builder()
                .message("successfully get non partnership company")
                .statusCode(HttpStatus.OK.value())
                .data(nonPartnerships)
                .build();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
