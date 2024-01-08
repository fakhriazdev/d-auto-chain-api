package com.danamon.autochain.dto.manage_user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchManageUserRequest {
    private Integer page;
    private Integer size;
    private String access;
    private String name;
}

