package com.kloudnuk.webserver.daos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.kloudnuk.webserver.daos.api.IAuthorityRepo;
import com.kloudnuk.webserver.models.Authority;

import java.util.stream.Stream;
import java.util.Optional;

@Repository("authorityRepo")
public class AuthorityRepoPostgres implements IAuthorityRepo {

    private JdbcTemplate jdbc;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    @Override
    public Stream<Authority> readAll() {
        return jdbc.queryForStream("select * from authorities",
                (rs, rowNum) -> new Authority(rs.getLong("id"), rs.getString("name")));
    }

    @Override
    public Optional<Authority> readByName(String name) {
        return Optional.ofNullable((Authority) jdbc.queryForObject(
                "select * from authorities " + "where name = \'" + name + "\';",
                (rs, rowNum) -> new Authority(rs.getLong("id"), rs.getString("name"))));
    }
}
