package com.kloudnuk.webserver.daos.api;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.List;

import com.kloudnuk.webserver.ddos.UserAuthorityDdo;
import com.kloudnuk.webserver.models.UserAuthority;

public interface IUserAuthorityRepo {

    Stream<UserAuthority> readByUser(String user);

    Stream<UserAuthority> readByRole(String role);

    Optional<UserAuthority> readById(long id);

    void insert(List<UserAuthorityDdo> userauthorities);

    void updateOne(String updateCol, String updateVal, String filterCol, String filterVal);

    void delete(String filterCol, String filterVal);
}
