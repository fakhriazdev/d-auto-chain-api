package com.danamon.autochain.constant;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum RoleType {
//    ========================= BACKOFFICE ROLE ======================
    SUPER_ADMIN,
    ADMIN,
    RELATIONSHIP_MANAGER,
    CREDIT_ANALYST,
//    ========================== USER ROLE =========================
    SUPER_USER,
    INVOICE_STAFF,
    FINANCE_STAFF,
    PAYMENT_STAFF;


    public String getName() {
        return this.name();
    }


    public static Map<String, List<String>> getAccessibility(List<RoleType> roleType){

        Map<String,List<String>> result = new HashMap<>();

        var create = "(Create)";
        var read = "(Read)";
        var update = "(Update)";
        var delete = "(Delete)";

        var dashboard = "Dashboard";
        var manageBoUser = "Manage Back Office User";
        var manageCompany = "Manage Company";
        var financing = "Financing";

        boolean equalAdmin = roleType.stream().anyMatch(role -> role.equals(RoleType.ADMIN));
        boolean equalCreditAnalyst = roleType.stream().anyMatch(role -> role.equals(RoleType.CREDIT_ANALYST));
        boolean equalRelationshipManager = roleType.stream().anyMatch(role -> role.equals(RoleType.RELATIONSHIP_MANAGER));
        if (equalAdmin && equalCreditAnalyst && equalRelationshipManager){
            result.put(dashboard,List.of(read));
            result.put(manageBoUser, List.of());
            result.put(manageCompany, List.of());
            result.put(financing,List.of(read,update));
        } else if (equalAdmin && equalCreditAnalyst) {
            result.put(dashboard,List.of(read));
            result.put(manageBoUser, List.of());
            result.put(manageCompany, List.of());
            result.put(financing, List.of(update, read));
        } else if (equalAdmin && equalRelationshipManager) {
            result.put(dashboard, List.of(read));
            result.put(manageCompany, List.of());
            result.put(financing, List.of(read));
        } else if (equalRelationshipManager && equalCreditAnalyst) {
            result.put(dashboard, List.of(read));
            result.put(manageCompany,List.of(read));
            result.put(financing,List.of(read,update));
        } else {
            result = getSingleRoleAccess(roleType.stream().findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.CONFLICT,"error")));
        };

        return result;
    }
    public static Map<String,List<String>> getSingleRoleAccess(RoleType roles){
        Map<String,List<String>> access = new HashMap<>();
        var create = "(Create)";
        var read = "(Read)";
        var update = "(Update)";
        var delete = "(Delete)";

        var dashboard = "Dashboard";
        var manageBoUser = "Manage Back Office User";
        var manageCompany = "Manage Company";
        var financing = "Financing";
        switch (roles){
            case ADMIN -> {
                access.put(dashboard, List.of(read));
                access.put(manageBoUser, List.of());
                access.put(manageCompany, List  .of());
            }
            case CREDIT_ANALYST -> {
                access.put(dashboard,List.of(read));
                access.put(manageCompany,List.of(read));
                access.put(financing,List.of(read,update));
            }
            case RELATIONSHIP_MANAGER -> {
                access.put(dashboard, List.of(read));
                access.put(manageCompany, List.of(read));
                access.put(financing, List.of(read));
            }
            default -> throw new ResponseStatusException(HttpStatus.CONFLICT, "Can`t Handle Type");
        };
        return access;
    }

}
