package com.kloudnuk.webserver.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

public class UserService implements UserDetailsService {

    final Logger log = LoggerFactory.getLogger(UserService.class);

    final String sql = "select users.name as username, "
            + "pgp_sym_decrypt(decode(users.password, 'hex')::bytea, \'"
            + System.getenv("KN_PGCRYPTOPASS") + "\') as password, " + "users.enabled as enabled, "
            + "authorities.name as role, " + "organizations.name as authority "
            + "from userauthorities " + "right join users on userauthorities.userid = users.id "
            + "left join authorities on userauthorities.authorityid = authorities.id "
            + "inner join organizations on users.organizationid = organizations.id";

    private JdbcTemplate jdbc;
    private PasswordEncoder encoder;

    public UserService(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    public UserDetails loadUserByUsername(String username) {
        Optional<UserAccess> user = Optional.ofNullable(
                (UserAccess) jdbc.queryForObject(sql + " where users.name = \'" + username + "\';",
                        (rs, rowNum) -> new UserAccess(rs.getString("username"),
                                rs.getString("password"), rs.getString("authority"),
                                rs.getString("role"), rs.getBoolean("enabled"))));

        UserAccess access = user.orElseThrow();
        StringBuilder sb = new StringBuilder();
        sb.append("username: " + access.username() + "\n");
        sb.append("password" + access.password() + "\n");
        sb.append("role: " + access.role() + "\n");
        sb.append("authority: " + access.authority() + "\n");
        sb.append("enabled: " + access.enabled());
        log.debug(sb.toString());
        return User.builder().passwordEncoder(encoder::encode).username(access.username())
                .password(access.password())
                .roles(access.authority(), access.username(), access.role())
                .disabled(!access.enabled()).build();
    }
}
