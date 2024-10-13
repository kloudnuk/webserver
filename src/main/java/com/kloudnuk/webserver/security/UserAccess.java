package com.kloudnuk.webserver.security;

public record UserAccess(String username, String password, String authority, String role,
        boolean enabled) {
}
