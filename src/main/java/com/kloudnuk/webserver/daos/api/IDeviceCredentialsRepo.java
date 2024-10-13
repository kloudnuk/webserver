package com.kloudnuk.webserver.daos.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.kloudnuk.webserver.models.DeviceCredentials;

public interface IDeviceCredentialsRepo {

    Optional<DeviceCredentials> readByControllerId(UUID id);

    void insert(List<DeviceCredentials> credentials);

    void updateOne(String updateCol, String updateVal, String filterCol, String filterVal);

    void delete(String filterCol, String filterVal);

}
