package com.danamon.autochain.controller;

import com.danamon.autochain.dto.DataResponse;
import com.danamon.autochain.dto.Invoice.request.RequestInvoice;
import com.danamon.autochain.dto.Invoice.response.InvoiceResponse;
import com.danamon.autochain.dto.PagingResponse;
import com.danamon.autochain.dto.manage_user.ManageUserResponse;
import com.danamon.autochain.dto.manage_user.NewUserRequest;
import com.danamon.autochain.dto.manage_user.SearchManageUserRequest;
import com.danamon.autochain.dto.manage_user.UpdateUserRequest;
import com.danamon.autochain.dto.payment.PaymentChangeMethodRequest;
import com.danamon.autochain.dto.payment.PaymentResponse;
import com.danamon.autochain.dto.payment.SearchPaymentRequest;
import com.danamon.autochain.service.ManageUserService;
import com.danamon.autochain.service.PaymentService;
import com.danamon.autochain.util.PagingUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manage-users")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class ManageUserController {
    private final ManageUserService manageUserService;

    @GetMapping
//    @PreAuthorize("hasAnyAuthority('INVOICE_STAFF','SUPER_USER','SUPER_ADMIN')")
    public ResponseEntity<?> getManageUser(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String access,
            @RequestParam(required = false) String name
    ){
        page = PagingUtil.validatePage(page);
        size = PagingUtil.validateSize(size);

        SearchManageUserRequest request = SearchManageUserRequest.builder()
                .page(page)
                .size(size)
                .access(access)
                .name(name )
                .build();

        Page<ManageUserResponse> users = manageUserService.getAllUser(request);

        PagingResponse pagingResponse = PagingResponse.builder()
                .count(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .page(page)
                .size(size)
                .build();

        DataResponse<List<ManageUserResponse>> response = DataResponse.<List<ManageUserResponse>>builder()
                .data(users.getContent())
                .paging(pagingResponse)
                .message("Success get users")
                .statusCode(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody NewUserRequest request){
        ManageUserResponse manageUserResponse = manageUserService.createUser(request);
        DataResponse<ManageUserResponse> response = DataResponse.<ManageUserResponse>builder()
                .data(manageUserResponse)
                .message("Success create user")
                .statusCode(HttpStatus.CREATED.value())
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody UpdateUserRequest request) {
        ManageUserResponse tableResponse = manageUserService.updateUser(request);
        DataResponse<ManageUserResponse> response = DataResponse.<ManageUserResponse>builder()
                .message("successfully edit user")
                .statusCode(HttpStatus.OK.value())
                .data(tableResponse)
                .build();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        ManageUserResponse userResponse = manageUserService.findById(id);
        DataResponse<ManageUserResponse> response = DataResponse.<ManageUserResponse>builder()
                .message("successfully get user")
                .statusCode(HttpStatus.OK.value())
                .data(userResponse)
                .build();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
