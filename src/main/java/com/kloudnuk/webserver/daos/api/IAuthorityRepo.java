package com.kloudnuk.webserver.daos.api;

import java.util.Optional;
import java.util.stream.Stream;

import com.kloudnuk.webserver.models.Authority;

public interface IAuthorityRepo {

    Stream<Authority> readAll();

    Optional<Authority> readByName(String name);

}
