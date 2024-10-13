package com.kloudnuk.webserver.models;

public record User(String name, String email, String orgname, String password, boolean enabled) {
}
