package com.kloudnuk.webserver.ddos;

import java.util.UUID;

import com.kloudnuk.webserver.enums.DeviceStatus;

public record Device(UUID controllerid, String name, String description, String ipaddress,
        String macaddress, DeviceStatus status) {
}
