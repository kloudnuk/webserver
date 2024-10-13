package com.kloudnuk.webserver.daos;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.kloudnuk.webserver.daos.api.IDeviceCredentialsRepo;
import com.kloudnuk.webserver.models.DeviceCredentials;

@Repository("credentialsRepo")
public class CredentialsRepoPostgres implements IDeviceCredentialsRepo {
    private JdbcTemplate jdbc;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    @Override
    public Optional<DeviceCredentials> readByControllerId(UUID id) {
        return Optional.ofNullable((DeviceCredentials) jdbc.queryForObject(
                "SELECT " + "controllerid, " + "sshprivatekey, " + "sshpublickey, "
                        + "wgprivatekey, " + "wgpublickey, " + "mongodbcertfs, "
                        + "pgp_sym_decrypt(decode(passwd, 'hex')::bytea, " + "\'"
                        + System.getenv("KN_PGCRYPTOPASS") + "\') as passwd "
                        + "FROM devicecredentials WHERE " + "controllerid = \'" + id.toString()
                        + "\';",
                (rs, rowNum) -> new DeviceCredentials(id, rs.getString("sshprivatekey"),
                        rs.getString("sshpublickey"), rs.getString("wgprivatekey"),
                        rs.getString("wgpublickey"), rs.getString("mongodbcertfs"),
                        rs.getString("passwd"))));
    }

    @Override
    public void insert(List<DeviceCredentials> credentials) {
        jdbc.batchUpdate(
                "INSERT INTO devicecredentials(" + "controllerid, " + "sshprivatekey, "
                        + "sshpublickey, " + "wgprivatekey, " + "wgpublickey, " + "mongodbcertfs, "
                        + "passwd) " + "VALUES (?,?,?,?,?,?," + "encode(pgp_sym_encrypt(?, " + "\'"
                        + System.getenv("KN_PGCRYPTOPASS") + "\'), 'hex'))",
                new BatchPreparedStatementSetter() {

                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setObject(1, credentials.get(i).controllerid());
                        ps.setString(2, credentials.get(i).sshPrivateKey());
                        ps.setString(3, credentials.get(i).sshPublicKey());
                        ps.setString(4, credentials.get(i).wgPrivateKey());
                        ps.setString(5, credentials.get(i).wgPublicKey());
                        ps.setString(6, credentials.get(i).mongodbCertfs());
                        ps.setString(7, credentials.get(i).passwd());
                    }

                    public int getBatchSize() {
                        return credentials.size();
                    }
                });
    }

    @Override
    public void updateOne(String updateCol, String updateVal, String col, String val) {
        jdbc.update("UPDATE devicecredentials " + "SET " + updateCol + " = \'" + updateVal + "\' "
                + "WHERE " + col + " = \'" + val + "\';");
    }

    @Override
    public void delete(String filterCol, String filterVal) {
        jdbc.update("DELETE FROM devicecredentials " + "WHERE " + filterCol + " = \'" + filterVal
                + "\';");
    }
}
