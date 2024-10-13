package com.kloudnuk.webserver.models;

import java.util.UUID;

public record DeviceCredentials(UUID controllerid, String sshPrivateKey, String sshPublicKey,
        String wgPrivateKey, String wgPublicKey, String mongodbCertfs, String passwd) {
}
