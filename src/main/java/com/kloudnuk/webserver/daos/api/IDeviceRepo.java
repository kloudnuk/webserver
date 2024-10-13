package com.kloudnuk.webserver.daos.api;

import java.util.stream.Stream;

import com.kloudnuk.webserver.ddos.DeviceDdo;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface IDeviceRepo {

    Stream<DeviceDdo> readAll();

    Optional<DeviceDdo> readById(UUID id);

    Optional<DeviceDdo> readByName(String name);

    void insert(List<DeviceDdo> devices);

    void updateOne(String updateCol, String updateVal, String filterCol, String filterVal);

    void delete(String filterCol, String filterVal);

    Stream<DeviceDdo> readByOrg(String org);

}
