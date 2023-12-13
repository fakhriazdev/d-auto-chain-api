package com.danamon.autochain.constant;

public enum BackofficeRoleType implements MasterRole{
    SUPER_ADMIN,
    ADMIN,
    RELATIONSHIP_MANAGER,
    CREDIT_ANALYST;

    @Override
    public String getName() {
        return this.name();
    }
}
