package com.kloudnuk.webserver.enums;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("DeviceStatus")
@Scope("prototype")
public enum DeviceStatus {
    INACTIVE, RUNNING, FAULT, OFFLINE
}
