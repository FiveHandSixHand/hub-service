package com.fhsh.daitda.common.config.security;

public class SecurityHeaderConstants {

    public static final String USER_ID = "X-User-Id";
    public static final String USER_EMAIL = "X-User-Email";
    public static final String USER_ROLE = "X-User-Role";

    private SecurityHeaderConstants() {
        throw new IllegalArgumentException("Utility class");
    }
}
