package com.kloudnuk.webserver.controllers;


import org.springframework.web.bind.annotation.RestController;

import com.kloudnuk.webserver.services.api.IDataStoreProvider;
import com.kloudnuk.webserver.enums.TimeQueryStyles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.ArrayList;

import org.bson.Document;

@RestController
@RequestMapping(path = "/api/v1/app")
public class ApplicationController {
  final Logger log = LoggerFactory.getLogger(ApplicationController.class);

  @Autowired
  IDataStoreProvider dsprovider;

  public ApplicationController(IDataStoreProvider dsprovider) {
    this.dsprovider = dsprovider;
  }

  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @GetMapping("/devices")
  @PreAuthorize("hasRole(#org) && hasRole('GUEST')")
  public List<Document> getAppDevices(@RequestParam @P("org") String org) {
    return dsprovider.getDeviceData(org, new String[0]).join();
  }

  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @GetMapping("/points")
  @PreAuthorize("hasRole(#org) && hasRole('GUEST')")
  public List<Document> getPointLists(@RequestParam @P("org") String org) {
    return dsprovider.getPointLists(org).join();
  }

  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @GetMapping("/deviceconfigurations")
  @PreAuthorize("hasRole(#org) && hasRole('CONTRIBUTOR')")
  public List<Document> getDeviceConfigurations(@RequestParam @P("org") String org) {
    return dsprovider.getDeviceConfigurations(org).join();
  }

  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @GetMapping("/devicelogs")
  @PreAuthorize("hasRole(#org) && hasRole('CONTRIBUTOR')")
  public List<Document> getDeviceLogs(@RequestParam @P("org") String org,
      @RequestParam String TimeQueryStyle, @RequestParam String TimeRangeOption) {

    List<Document> logs = new ArrayList<Document>();
    switch (TimeQueryStyles.valueOf(TimeQueryStyle)) {
      case regex:
        try {
          logs = dsprovider.getDeviceLogsByRegex(org, TimeRangeOption).join();
        } catch (Exception e) {
          log.error(e.getLocalizedMessage(), e);
        }
        break;
      case options:
        // TODO
        log.info("options TODO");
        break;
      default:
        log.info("unknown time-query-style...");
    }
    return logs;
  }
}
