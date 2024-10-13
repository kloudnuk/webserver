package com.kloudnuk.webserver.services.api;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.core.io.Resource;

import com.kloudnuk.webserver.models.SoftwarePackage;

public interface IDistributionProvider {

    public List<SoftwarePackage> listPackages();

    public Optional<Resource> getPackageAsResource(String packageName) throws IOException;
}
