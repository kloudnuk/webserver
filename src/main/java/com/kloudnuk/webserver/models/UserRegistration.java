package com.kloudnuk.webserver.models;

public record UserRegistration(String name, String email, String password, boolean enabled,
                String role) {

}
