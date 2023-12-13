package com.danamon.autochain.constant;

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
}
