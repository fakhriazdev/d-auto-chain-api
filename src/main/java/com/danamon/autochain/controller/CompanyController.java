package com.danamon.autochain.controller;

import com.danamon.autochain.dto.DataResponse;
import com.danamon.autochain.dto.PagingResponse;
import com.danamon.autochain.dto.company.NewCompanyRequest;
import com.danamon.autochain.dto.company.NewCompanyResponse;
import com.danamon.autochain.dto.company.SearchCompanyRequest;
import com.danamon.autochain.dto.company.CompanyResponse;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.util.PagingUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createMenu(
            @RequestParam String companyName,
            @RequestParam String province,
            @RequestParam String city,
            @RequestParam String address,
            @RequestParam String phoneNumber,
            @RequestParam String companyEmail,
            @RequestParam String accountNumber,
            @RequestParam Double financingLimit,
            @RequestParam Double remainingLimit,
            @RequestParam MultipartFile[] files,
            @RequestParam String username
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
                .multipartFiles(List.of(files))
                .username(username)
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
}
