package com.kloudnuk.webserver.models;

import java.util.Date;

public record SoftwarePackage(String name, String version, Date releaseDate, String location) {
}
