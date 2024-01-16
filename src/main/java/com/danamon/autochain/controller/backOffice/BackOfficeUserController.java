package com.danamon.autochain.controller.backOffice;

import com.danamon.autochain.constant.RoleType;
import com.danamon.autochain.dto.DataResponse;
import com.danamon.autochain.dto.Mono;
import com.danamon.autochain.dto.PagingResponse;
import com.danamon.autochain.dto.auth.BackOfficeRegisterRequest;
import com.danamon.autochain.dto.auth.BackOfficeRegisterResponse;
import com.danamon.autochain.dto.backoffice.BackOfficeUserRequest;
import com.danamon.autochain.dto.backoffice.BackOfficeUserResponse;
import com.danamon.autochain.dto.backoffice.BackOfficeViewResponse;
import com.danamon.autochain.dto.backoffice.BackofficeRolesResponse;
import com.danamon.autochain.dto.company.CompanyResponse;
import com.danamon.autochain.dto.company.SearchCompanyRequest;
import com.danamon.autochain.service.BackOfficeUserService;
import com.danamon.autochain.service.CompanyService;
import com.danamon.autochain.util.PagingUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/backoffice/users")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
public class BackOfficeUserController {
    private final BackOfficeUserService backOfficeUserService;
    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(required = false, defaultValue = "1") Integer page,
                                    @RequestParam(required = false, defaultValue = "10") Integer size,
                                    @RequestParam(required = false, defaultValue = "asc") String direction,
                                    @RequestParam(required = false) String role){
        page = PagingUtil.validatePage(page);
        size = PagingUtil.validateSize(size);
        direction = PagingUtil.validateDirection(direction);

        BackOfficeUserRequest request = BackOfficeUserRequest.builder()
                .role(role)
                .direction(direction)
                .page(page)
                .size(size)
                .build();

        Page<BackOfficeUserResponse> allBackOfficeUser = backOfficeUserService.getAllBackOfficeUser(request);

        PagingResponse pagingResponse = PagingResponse.builder()
                .count(allBackOfficeUser.getTotalElements())
                .totalPages(allBackOfficeUser.getTotalPages())
                .page(page)
                .size(size)
                .build();

        DataResponse<List<BackOfficeUserResponse>> successGetAllData = DataResponse.<List<BackOfficeUserResponse>>builder()
                .message("Success Get All Data")
                .statusCode(HttpStatus.OK.value())
                .data(allBackOfficeUser.getContent())
                .paging(pagingResponse)
                .build();
        return ResponseEntity.ok(successGetAllData);
    }

    @GetMapping("/view/relationship-manager")
    private ResponseEntity<?> getRelationshipManagerView(@RequestParam(required = false, defaultValue = "1") Integer page,
                                                         @RequestParam(required = false, defaultValue = "10") Integer size,
                                                         @RequestParam(required = false, defaultValue = "asc") String direction,
                                                         @RequestParam(required = false) String status,
                                                         @RequestParam(required = false) String name){
        // set up request
        SearchCompanyRequest searchCompanyRequest = SearchCompanyRequest.builder()
                .name(name)
                .sortBy("companyName")
                .direction(direction)
                .page(page)
                .size(size)
                .status(status)
                .build();
        Page<CompanyResponse> companies = companyService.getAll(searchCompanyRequest);

        PagingResponse pagingResponse = PagingResponse.builder()
                .count(companies.stream().count())
                .size(companies.getSize())
                .page(companies.getTotalPages())
                .build();

        DataResponse<List<CompanyResponse>> response = DataResponse.<List<CompanyResponse>>builder()
                .paging(pagingResponse)
                .data(companies.getContent())
                .statusCode(HttpStatus.OK.value())
                .message("Success get data")
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/view/accessibility")
    public ResponseEntity<?> getUserAccessibility(@RequestBody Mono<List<String>> request){
        List<RoleType> collect = request.getMono().stream().map(RoleType::valueOf).toList();
        BackOfficeViewResponse<HashMap<String, List<String>>> accessibility = (BackOfficeViewResponse<HashMap<String, List<String>>>) backOfficeUserService.getAccessibility(collect);

        DataResponse<BackOfficeViewResponse<HashMap<String, List<String>>>> successGetAllData = DataResponse.<BackOfficeViewResponse<HashMap<String, List<String>>>>builder()
                .message("Success Get All Data")
                .data(accessibility)
                .statusCode(HttpStatus.OK.value())
                .build();


        return ResponseEntity.ok(successGetAllData);
    }

    @PostMapping
    public ResponseEntity<?> addUser(@RequestBody BackOfficeRegisterRequest backOfficeRegisterRequest){
        BackOfficeRegisterResponse backOfficeRegisterResponse = backOfficeUserService.addBackOfficeUser(backOfficeRegisterRequest);

        DataResponse<BackOfficeRegisterResponse> successRegisterUser = DataResponse.<BackOfficeRegisterResponse>builder()
                .statusCode(HttpStatus.CREATED.value())
                .data(backOfficeRegisterResponse)
                .message("Success Register User")
                .build();

        return ResponseEntity.ok(successRegisterUser);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getUserInfo(@PathVariable(name = "id") String id){
        BackOfficeUserResponse backOfficeUserById = backOfficeUserService.getBackOfficeUserById(id);

        DataResponse<BackOfficeUserResponse> response = DataResponse.<BackOfficeUserResponse>builder()
                .data(backOfficeUserById)
                .message("Success Get Data")
                .statusCode(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/edit")
    public ResponseEntity<?> editBackofficeUser(@RequestBody EditBackOfficeUser editBackOfficeUser){
        backOfficeUserService.updateBackofficeUser(editBackOfficeUser);
        return ResponseEntity.ok("Success Edit User");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteBackOfficeUser(@PathVariable(name = "id") String id){
        backOfficeUserService.deleteUser(id);

        return ResponseEntity.ok("Success Delete User");
    }

    @GetMapping("/roles")
    public ResponseEntity<?> getBackofficeRoles(){
        List<BackofficeRolesResponse> backOfficeRoles = backOfficeUserService.getBackOfficeRoles();
        DataResponse<List<BackofficeRolesResponse>> response
                = DataResponse.<List<BackofficeRolesResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Success")
                .data(backOfficeRoles)
                .build();

        return ResponseEntity.ok(response);
    }

    public record EditBackOfficeUser(String id, String username, String name, String email, List<String> roles, List<String> companies){
    }

}
