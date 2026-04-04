package com.fhsh.daitda.common.enums;

import com.fhsh.daitda.common.exception.InvalidAuthenticationHeaderException;

import java.util.Arrays;

public enum UserRole {
    ADMIN,
    HUB_ADMIN,
    DELIVERY,
    COMPANY;

    public static UserRole from(String role) {
        if (role == null || role.isBlank()) {
            throw new InvalidAuthenticationHeaderException("사용자 권한 헤더가 존재하지 않습니다.");
        }

        return Arrays.stream(values())
                .filter(userRole -> userRole.name().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new InvalidAuthenticationHeaderException("지원하지 않는 사용자 권한입니다."));
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isHubAdmin() {
        return this == HUB_ADMIN;
    }
}
