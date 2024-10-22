package com.kloudnuk.webserver.daos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;
import java.util.Optional;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.kloudnuk.webserver.daos.api.IUserRepo;
import com.kloudnuk.webserver.ddos.UserDdo;
import com.kloudnuk.webserver.models.User;

@Repository("userRepo")
public class UserRepoPostgres implements IUserRepo {
        private JdbcTemplate jdbc;

        @Autowired
        public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
                this.jdbc = jdbcTemplate;
        }

        @Override
        public Stream<User> readAll(String org) {
                String sql1 = """
                                select
                                users.name,
                                users.email,
                                users.enabled,
                                organizations.name as org,
                                """;
                String sql2 = "users.password as password ";
                String sql3 = """
                                from users
                                right join organizations
                                    on users.organizationid = organizations.id
                                """;
                String sql4 = " where organizations.name = \'" + org + "\';";

                return jdbc.queryForStream(sql1 + sql2 + sql3 + sql4,
                                (rs, rowNum) -> new User(rs.getString("name"),
                                                rs.getString("email"), rs.getString("org"),
                                                rs.getString("password"),
                                                rs.getBoolean("enabled")));
        }

        @Override
        public Optional<User> readById(long id) {
                String sql1 = """
                                select
                                users.name,
                                users.email,
                                users.enabled,
                                organizations.name as org,
                                """;
                String sql2 = "users.password as password ";
                String sql3 = """
                                from users
                                right join organizations
                                    on users.organizationid = organizations.id
                                """;
                String sql4 = "where users.id = \'" + Long.valueOf(id) + "\';";

                return Optional.ofNullable((User) jdbc.queryForObject(sql1 + sql2 + sql3 + sql4,
                                (rs, rowNum) -> new User(rs.getString("name"),
                                                rs.getString("email"), rs.getString("org"),
                                                rs.getString("password"),
                                                rs.getBoolean("enabled"))));
        }

        @Override
        public Optional<User> readByName(String name) {
                String sql1 = """
                                select
                                users.name,
                                users.email,
                                users.enabled,
                                organizations.name as org,
                                """;
                // String sql2 =
                // "pgp_sym_decrypt(decode(users.password,
                // 'hex')::bytea, \'"
                // + System.getenv("KN_PGCRYPTOPASS")
                // + "\') as password ";
                String sql2 = "users.password as password ";
                String sql3 = """
                                from users
                                right join organizations
                                    on users.organizationid = organizations.id
                                """;
                String sql4 = "where users.name = \'" + name + "\';";

                return Optional.ofNullable((User) jdbc.queryForObject(sql1 + sql2 + sql3 + sql4,
                                (rs, rowNum) -> new User(rs.getString("name"),
                                                rs.getString("email"), rs.getString("org"),
                                                rs.getString("password"),
                                                rs.getBoolean("enabled"))));
        }

        @Override
        public void insert(List<UserDdo> users, Long orgid) {
                String sql1 = """
                                INSERT INTO users(
                                    name,
                                    email,
                                    organizationid,
                                    password,
                                    enabled)
                                    VALUES (?,?,?,
                                """;
                String sql2 = " encode(pgp_sym_encrypt(?, " + "\'"
                                + System.getenv("KN_PGCRYPTOPASS") + "\'), 'hex'),?)";

                jdbc.batchUpdate(sql1 + sql2, new BatchPreparedStatementSetter() {

                        @SuppressWarnings("null")
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                                ps.setString(1, users.get(i).name());
                                ps.setString(2, users.get(i).email());
                                ps.setLong(3, orgid);
                                ps.setString(4, users.get(i).password());
                                ps.setBoolean(5, users.get(i).enabled());

                        }

                        public int getBatchSize() {
                                return users.size();
                        }
                });
        }

        @Override
        public void updateOne(String updateCol, String updateVal, String col, String val) {
                jdbc.update("UPDATE users " + "SET " + updateCol + " = \'" + updateVal + "\' "
                                + "WHERE " + col + " = \'" + val + "\';");
        }

        @Override
        public void updatePassword(String user, String secret) {
                String sql1 = """
                                update users
                                set password =
                                """;
                String sql2 = " encode(pgp_sym_encrypt(" + "\'" + secret + "\'" + ", \'"
                                + System.getenv("KN_PGCRYPTOPASS") + "\'), 'hex')";
                String sql3 = " where name = \'" + user + "\';";

                try {
                        jdbc.update(sql1 + sql2 + sql3);
                } catch (Exception e) {
                        System.out.println(e.getMessage());
                }

        }

        @Override
        public void delete(String filterCol, String filterVal) {
                jdbc.update("DELETE FROM users " + "WHERE " + filterCol + " = \'" + filterVal
                                + "\';");
        }

        @Override
        public Long getUserId(String username) {
                return Optional.ofNullable((Long) jdbc.queryForObject(
                                "select id from users where users.name = \'" + username + "\'",
                                Long.class)).orElseThrow();
        }
}
