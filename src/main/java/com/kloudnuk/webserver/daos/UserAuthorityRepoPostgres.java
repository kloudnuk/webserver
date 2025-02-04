package com.kloudnuk.webserver.daos;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.kloudnuk.webserver.daos.api.IUserAuthorityRepo;
import com.kloudnuk.webserver.ddos.UserAuthorityDdo;
import com.kloudnuk.webserver.models.UserAuthority;

@Repository("userauthRepo")
public class UserAuthorityRepoPostgres implements IUserAuthorityRepo {
    private JdbcTemplate jdbc;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    @Override
    public Stream<UserAuthority> readByUser(String username) {
        String sql1 = """
                    select
                    users.name as username,
                    authorities.name as role
                from userauthorities
                    right join users
                        on userauthorities.userid = users.id
                    right join authorities
                        on userauthorities.authorityid = authorities.id
                """;
        String sql2 = "where users.name = \'" + username + "\';";

        return jdbc.queryForStream(sql1 + sql2,
                (rs, rowNum) -> new UserAuthority(rs.getString("username"), rs.getString("role")));
    }

    @Override
    public Stream<UserAuthority> readByRole(String role) {
        String sql1 = """
                    select
                    users.name as username,
                    authorities.name as role
                from userauthorities
                    right join users
                        on userauthorities.userid = users.id
                    right join authorities
                        on userauthorities.authorityid = authorities.id
                """;
        String sql2 = "where authorities.name = \'" + role + "\';";

        return jdbc.queryForStream(sql1 + sql2,
                (rs, rowNum) -> new UserAuthority(rs.getString("username"), rs.getString("role")));
    }

    @Override
    public Optional<UserAuthority> readById(long id) {
        String sql1 = """
                    select
                    users.name as username,
                    authorities.name as role
                from userauthorities
                    right join users
                        on userauthorities.userid = users.id
                    right join authorities
                        on userauthorities.authorityid = authorities.id
                """;
        String sql2 = "where userauthorities.id = " + id + ";";

        return Optional.ofNullable((UserAuthority) jdbc.queryForObject(sql1 + sql2,
                (rs, rowNum) -> new UserAuthority(rs.getString("username"), rs.getString("role"))));
    }

    @Override
    public void insert(List<UserAuthorityDdo> userauthorities) {
        String sql = """
                INSERT INTO userauthorities(
                    userid,
                    authorityid)
                    VALUES (?,?)
                """;

        jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @SuppressWarnings("null")
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, userauthorities.get(i).userid());
                ps.setLong(2, userauthorities.get(i).authorityid());
            }

            public int getBatchSize() {
                return userauthorities.size();
            }
        });
    }

    @Override
    public void updateOne(String updateCol, String updateVal, String col, String val) {
        jdbc.update("UPDATE userauthorities " + "SET " + updateCol + " = \'" + updateVal + "\' "
                + "WHERE " + col + " = \'" + val + "\';");
    }

    @Override
    public void delete(String filterCol, String filterVal) {
        jdbc.update("DELETE FROM userauthorities " + "WHERE " + filterCol + " = \'" + filterVal
                + "\';");
    }
}
