package com.brandex.models.enums;

public enum UserStatus {
    ACTIVE("Active"),
    PENDING("Pending"),
    INACTIVE("Inactive"),
    SUSPENDED("Suspended"),
    BANNED("Banned");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
