package com.kloudnuk.webserver.daos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.Optional;

import com.kloudnuk.webserver.daos.api.IDeviceRepo;
import com.kloudnuk.webserver.ddos.DeviceDdo;
import com.kloudnuk.webserver.enums.DeviceStatus;

@Repository("deviceRepo")
public class DeviceRepoPostgres implements IDeviceRepo {

    private JdbcTemplate jdbc;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    @Override
    public Stream<DeviceDdo> readAll() {
        return jdbc.queryForStream("select * from devices",
                (rs, rowNum) -> new DeviceDdo((UUID) rs.getObject("controllerid"),
                        rs.getString("name"), rs.getString("description"),
                        rs.getString("ipaddress"), rs.getString("macaddress"),
                        DeviceStatus.valueOf(rs.getString("status")), rs.getString("gateway"),
                        rs.getString("wgaddress"), rs.getLong("organizationid")));
    }

    @Override
    public void insert(List<DeviceDdo> devices) {

        jdbc.batchUpdate(
                "INSERT INTO devices(" + "controllerid, " + "name, " + "description, "
                        + "ipaddress, " + "macaddress, " + "status, " + "gateway, " + "wgaddress, "
                        + "organizationid) " + "VALUES (?,?,?,?,?,?,?,?,?)",
                new BatchPreparedStatementSetter() {

                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setObject(1, devices.get(i).controllerid());
                        ps.setString(2, devices.get(i).name());
                        ps.setString(3, devices.get(i).description());
                        ps.setString(4, devices.get(i).ipaddress());
                        ps.setString(5, devices.get(i).macaddress());
                        ps.setString(6, devices.get(i).status().toString());
                        ps.setString(7, devices.get(i).gateway());
                        ps.setString(8, devices.get(i).wgaddress());
                        ps.setLong(9, devices.get(i).orgid());
                    }

                    public int getBatchSize() {
                        return devices.size();
                    }
                });
    }

    @Override
    public void updateOne(String updateCol, String updateVal, String col, String val) {
        jdbc.update("UPDATE devices " + "SET " + updateCol + " = \'" + updateVal + "\' " + "WHERE "
                + col + " = \'" + val + "\';");
    }

    @Override
    public void delete(String filterCol, String filterVal) {
        jdbc.update("DELETE FROM devices " + "WHERE " + filterCol + " = \'" + filterVal + "\';");
    }

    @Override
    public Optional<DeviceDdo> readById(UUID id) {
        return Optional.ofNullable((DeviceDdo) jdbc.queryForObject(
                "SELECT * FROM devices WHERE " + "controllerid = \'" + id.toString() + "\';",
                (rs, rowNum) -> new DeviceDdo((UUID) rs.getObject("controllerid"),
                        rs.getString("name"), rs.getString("description"),
                        rs.getString("ipaddress"), rs.getString("macaddress"),
                        DeviceStatus.valueOf(rs.getString("status")), rs.getString("gateway"),
                        rs.getString("wgaddress"), rs.getLong("organizationid"))));
    }

    @Override
    public Optional<DeviceDdo> readByName(String name) {
        return Optional.ofNullable((DeviceDdo) jdbc.queryForObject(
                "SELECT * FROM devices WHERE " + "name = \'" + name + "\';",
                (rs, rowNum) -> new DeviceDdo((UUID) rs.getObject("controllerid"),
                        rs.getString("name"), rs.getString("description"),
                        rs.getString("ipaddress"), rs.getString("macaddress"),
                        DeviceStatus.valueOf(rs.getString("status")), rs.getString("gateway"),
                        rs.getString("wgaddress"), rs.getLong("organizationid"))));
    }

    @Override
    public Stream<DeviceDdo> readByOrg(String name) {
        String sql1 = """
                   select
                   devices.controllerid,
                   devices.name,
                   devices.description,
                   devices.ipaddress,
                   devices.macaddress,
                   devices.status,
                   devices.gateway,
                   devices.wgaddress,
                   organizations.id as organizationid
                from devices
                   right join organizations
                       on devices.organizationid = organizations.id
                       """;
        String sql2 = " where organizations.name = \'" + name + "\';";

        return jdbc.queryForStream(sql1 + sql2,
                (rs, rowNum) -> new DeviceDdo((UUID) rs.getObject("controllerid"),
                        rs.getString("name"), rs.getString("description"),
                        rs.getString("ipaddress"), rs.getString("macaddress"),
                        DeviceStatus.valueOf(rs.getString("status")), rs.getString("gateway"),
                        rs.getString("wgaddress"), rs.getLong("organizationid")));
    }
}
