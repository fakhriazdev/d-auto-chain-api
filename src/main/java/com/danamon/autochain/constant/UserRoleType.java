package com.danamon.autochain.constant;


public enum UserRoleType implements MasterRole {
    SUPER_ADMIN,
    ADMIN;

    @Override
    public String getName() {
        return this.name();
    }
}
