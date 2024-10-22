package com.kloudnuk.webserver.daos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.kloudnuk.webserver.daos.api.IOrgRepo;
import com.kloudnuk.webserver.models.Org;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.List;
import java.util.stream.Stream;
import java.util.Optional;

@Repository("orgRepo")
public class OrgRepoPostgres implements IOrgRepo {

    private JdbcTemplate jdbc;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    public Stream<Org> readAll() {
        return jdbc.queryForStream("select * from organizations",
                (rs, rowNum) -> new Org(rs.getString("name")));
    }

    public void create(List<Org> orgs) {
        jdbc.batchUpdate("INSERT INTO organizations(" + "name) " + "VALUES (?)",
                new BatchPreparedStatementSetter() {

                    @SuppressWarnings("null")
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, orgs.get(i).name());
                    }

                    public int getBatchSize() {
                        return orgs.size();
                    }
                });
    }

    @Override
    public void updateOne(String updateCol, String updateVal, String col, String val) {
        jdbc.update("UPDATE organizations " + "SET " + updateCol + " = \'" + updateVal + "\' "
                + "WHERE " + col + " = \'" + val + "\';");
    }

    @Override
    public void delete(String filterCol, String filterVal) {
        jdbc.update(
                "DELETE FROM organizations " + "WHERE " + filterCol + " = \'" + filterVal + "\';");
    }

    @Override
    public Optional<Org> readById(int id) {
        return Optional.ofNullable((Org) jdbc.queryForObject(
                "SELECT * FROM organizations WHERE " + "ID = \'" + Integer.toString(id) + "\';",
                (rs, rowNum) -> new Org(rs.getString("name"))));
    }

    @Override
    public Long getOrgId(String orgname) {
        return Optional.ofNullable((Long) jdbc.queryForObject(
                "select id from organizations where name = \'" + orgname + "\';", Long.class))
                .orElseThrow();
    }
}
