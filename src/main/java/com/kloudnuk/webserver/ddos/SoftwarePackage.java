package com.kloudnuk.webserver.ddos;

import java.util.Date;

public record SoftwarePackage(Long id, String name, String version, Date releaseDate,
                String location) {

}
