package com.fhsh.daitda.common.model;

import com.fhsh.daitda.common.enums.UserRole;

public record AuthenticatedUser(
        String userId,
        String email,
        UserRole role
) {
    public static AuthenticatedUser fromHeaders(String userId, String email, String role) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("사용자 식별 헤더가 존재하지 않습니다.");
        }

        return new AuthenticatedUser(
                userId,
                email != null ? email : "",
                UserRole.from(role)
        );
    }

    public boolean isAdmin() {
        return role.isAdmin();
    }

    public boolean isHubAdmin() {
        return role.isHubAdmin();
    }
}
