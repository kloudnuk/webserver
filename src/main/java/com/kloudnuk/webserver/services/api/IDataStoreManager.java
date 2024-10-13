package com.kloudnuk.webserver.services.api;

import java.io.File;

public interface IDataStoreManager {

    // org = database name, deviceuuid = username
    public void createDbUser(String org, String deviceuuid);

    public File createX509Cert(String deviceuuid, int expMonths) throws NullPointerException;

    public void removeDbUser(String uuid) throws NullPointerException;
}
