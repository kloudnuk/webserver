package com.kloudnuk.webserver.ddos;

public record UserDdo(String name, String email, Long orgid, String password, boolean enabled) {
}
