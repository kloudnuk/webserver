package com.kloudnuk.webserver.daos.api;

import java.util.stream.Stream;

import com.kloudnuk.webserver.models.Org;

import java.util.Optional;
import java.util.List;

public interface IOrgRepo {

    Stream<Org> readAll();

    Optional<Org> readById(int id);

    void create(List<Org> orgs);

    void updateOne(String updateCol, String updateVal, String filterCol, String filterVal);

    void delete(String filterCol, String filterVal);

    public Long getOrgId(String orgname);

}
