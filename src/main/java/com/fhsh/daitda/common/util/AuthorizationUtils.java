package com.fhsh.daitda.common.util;

import com.fhsh.daitda.common.enums.UserRole;
import com.fhsh.daitda.common.exception.ForbiddenException;
import com.fhsh.daitda.common.model.AuthenticatedUser;

public final class AuthorizationUtils {

    private AuthorizationUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void validateMasterAccess(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            throw new ForbiddenException("권한 정보가 존재하지 않습니다.");
        }

        if (!authenticatedUser.isAdmin()) {
            throw new ForbiddenException("해당 요청에 대한 권한이 없습니다.");
        }
    }

    public static void validateAllAccess(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            throw new ForbiddenException("권한 정보가 존재하지 않습니다.");
        }

        UserRole role = authenticatedUser.role();

        if (!(role.isAdmin()
                || role.isHubAdmin()
                || role.isDelivery()
                || role.isCompany())) {
            throw new ForbiddenException("해당 요청에 대한 권한이 없습니다.");
        }
    }
}
