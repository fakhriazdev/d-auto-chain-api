package com.danamon.autochain.dto.manage_user;

import com.danamon.autochain.dto.Invoice.response.InvoiceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ManageUserResponse {
    private String username;
    private String name;
    private String email;
    private List<String> access;
}
