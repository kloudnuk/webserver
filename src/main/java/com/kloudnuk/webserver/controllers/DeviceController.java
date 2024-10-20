package com.kloudnuk.webserver.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import com.kloudnuk.webserver.daos.api.IDeviceRepo;
import com.kloudnuk.webserver.daos.api.IOrgRepo;
import com.kloudnuk.webserver.ddos.DeviceDdo;
import com.kloudnuk.webserver.models.Device;
import com.kloudnuk.webserver.models.EntityUpdate;
import com.kloudnuk.webserver.services.api.IDataStoreManager;
import com.kloudnuk.webserver.services.api.IDistributionProvider;

import java.net.URI;

import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping(path = "/api/v1/devices")
public class DeviceController {
    final Logger log = LoggerFactory.getLogger(DeviceController.class);

    @Autowired
    private IDistributionProvider distributor;

    private IDataStoreManager dsmanager;

    private IDeviceRepo repo;

    private IOrgRepo orgRepo;

    public DeviceController(IDeviceRepo repo, IOrgRepo orgRepo, IDataStoreManager dsmanager) {
        this.repo = repo;
        this.orgRepo = orgRepo;
        this.dsmanager = dsmanager;
    }

    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @GetMapping("/")
    @PreAuthorize("hasRole(#org) && hasRole('CONTRIBUTOR')")
    public List<Device> readAll(@RequestParam @P("org") String org) {
        List<DeviceDdo> ddoList = repo.readByOrg(org).toList();
        return ddoList.stream()
                .map(ddo -> new Device(ddo.controllerid(), ddo.name(), ddo.description(),
                        ddo.ipaddress(), ddo.macaddress(), ddo.status(), ddo.gateway(),
                        ddo.wgaddress()))
                .collect(Collectors.toList());
    }

    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @GetMapping("/{controllerid}")
    @PreAuthorize("hasRole(#org) && hasRole('CONTRIBUTOR')")
    public DeviceDdo readById(@RequestParam @P("org") String org,
            @PathVariable String controllerid) {
        return repo.readById(UUID.fromString(controllerid)).get();
    }

    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @GetMapping("/name/{name}")
    @PreAuthorize("hasRole(#org) && hasRole('CONTRIBUTOR')")
    public DeviceDdo readByName(@RequestParam @P("org") String org, @PathVariable String name) {
        return repo.readByName(name).get();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @PostMapping("/enroll")
    @PreAuthorize("hasRole(#org) && hasRole('CONTRIBUTOR')")
    public List<Device> addDevices(@RequestBody List<Device> devices,
            @RequestParam @P("org") String org) {

        Long orgid = orgRepo.getOrgId(org);
        List<DeviceDdo> deviceDdos = devices.stream()
                .map(device -> new DeviceDdo(device.controllerid(), device.name(),
                        device.description(), device.ipaddress(), device.macaddress(),
                        device.status(), device.gateway(), device.wgaddress(), orgid))
                .collect(Collectors.toList());
        repo.insert(deviceDdos);
        deviceDdos.forEach(device -> dsmanager.createDbUser(org, device.controllerid().toString()));
        return devices;
    }

    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PostMapping("/activate")
    @PreAuthorize("hasRole(#org) && hasRole('CONTRIBUTOR')")
    public ResponseEntity<?> enableDevice(@RequestParam String softwarePackage,
            @RequestParam UUID deviceid, @RequestParam @P("org") String org) {

        Optional<Resource> resource;

        Optional<DeviceDdo> device =
                repo.readAll().filter(dev -> dev.controllerid().equals(deviceid)).findFirst();

        log.info("device found: ".concat(device.get().toString()));

        if (device.isPresent()) {
            try {
                resource = distributor.getPackageAsResource(softwarePackage);
                resource.ifPresent(res -> {
                    try {
                        Map<String, String> env = new HashMap<>();
                        env.put("create", "true");
                        File cert = dsmanager.createX509Cert(deviceid.toString(), 3);
                        Path zipFilePath = Paths.get(res.getFile().getAbsolutePath());
                        URI zipUri = URI.create("jar:" + zipFilePath.toUri());
                        try (FileSystem fs = FileSystems.newFileSystem(zipUri, env)) {
                            Path zipCertPath = fs.getPath("cert.pem");
                            Files.write(zipCertPath, Files.readAllBytes(cert.toPath()),
                                    StandardOpenOption.CREATE);
                        } catch (Exception e) {
                            log.error(e.getLocalizedMessage(), e);
                        }
                    } catch (IOException ioe) {
                        log.error(ioe.getLocalizedMessage(), ioe);
                    }
                });
                String contentType = "application/octet-stream";
                String headerValue =
                        "attachment; filename=\"" + resource.get().getFilename() + "\"";
                return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, headerValue).body(resource.get());
            } catch (IOException ioe) {
                log.error("IO package access error (/activate)", ioe);
                return ResponseEntity.internalServerError().build();
            } catch (Exception e) {
                log.error("generic error (/activate)", e);
                return ResponseEntity.internalServerError().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/remove")
    @PreAuthorize("hasRole(#org) && hasRole('CONTRIBUTOR')")
    public ResponseEntity<?> deleteDevice(@RequestParam UUID deviceid,
            @RequestParam @P("org") String org) {
        try {
            // credentialsRepo.delete("controllerid", deviceid.toString());
            repo.delete("controllerid", deviceid.toString());
            dsmanager.removeDbUser(deviceid.toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping("edit/{controllerid}")
    @PreAuthorize("hasRole(#org) && hasRole('CONTRIBUTOR')")
    public ResponseEntity<?> putMethodName(@PathVariable String controllerid,
            @RequestParam @P("org") String org, @RequestBody EntityUpdate update) {
        try {
            repo.updateOne(update.updateColumn(), update.updateValue(), update.filterColumn(),
                    update.filterValue());
        } catch (Exception e) {
            log.error("Data Access Layer Error... ", e);
            ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }
}
