package com.danamon.autochain.controller;

import com.danamon.autochain.dto.request.SearchCompanyRequest;
import com.danamon.autochain.dto.response.CommonResponse;
import com.danamon.autochain.dto.response.CompanyResponse;
import com.danamon.autochain.dto.response.PagingResponse;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.util.PagingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
//@SecurityRequirement(name = "Bearer Authentication")
public class CompanyController {
    private final CompanyService companyService;

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
        CommonResponse<List<CompanyResponse>> response = CommonResponse.<List<CompanyResponse>>builder()
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
