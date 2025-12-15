package com.emsi.employe_service.security;

public class SecurityUtils {

    public static boolean isAdmin(String role) {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public static boolean isEmploye(String role) {
        return "EMPLOYE".equalsIgnoreCase(role);
    }
}
