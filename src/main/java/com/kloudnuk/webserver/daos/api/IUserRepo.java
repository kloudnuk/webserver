package com.kloudnuk.webserver.daos.api;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.kloudnuk.webserver.ddos.UserDdo;
import com.kloudnuk.webserver.models.User;

public interface IUserRepo {

    Stream<User> readAll(String org);

    Optional<User> readById(long id);

    Optional<User> readByName(String name);

    void insert(List<UserDdo> users, Long orgid);

    void updateOne(String updateCol, String updateVal, String filterCol, String filterVal);

    void updatePassword(String user, String secret);

    void delete(String filterCol, String filterVal);

    public Long getUserId(String username);
}
