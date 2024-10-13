package com.kloudnuk.webserver.services;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.stereotype.Component;

@Component
public class ApplicationProps {

  private final String path, ownerid, orgid, privkey, pubkey, host, uri, connectionstring,
      projectid;

  public ApplicationProps() {
    this.path = "/nuk/application.properties";
    Properties properties = new Properties();
    try (InputStream input = new FileInputStream("/nuk/application.properties");) {
      properties.load(input);
    } catch (Exception e) {
      e.printStackTrace();
    }
    this.ownerid = properties.getProperty("ds.ownerid");
    this.orgid = properties.getProperty("ds.orgid");
    this.privkey = properties.getProperty("ds.privatekey");
    this.pubkey = properties.getProperty("ds.publickey");
    this.host = properties.getProperty("host");
    this.uri = properties.getProperty("ds.uri");
    this.connectionstring = properties.getProperty("ds.connectionstring");
    this.projectid = properties.getProperty("ds.projectid");
  }

  public ApplicationProps(String path) {
    this.path = path;
    Properties properties = new Properties();
    try (InputStream input = new FileInputStream("/nuk/application.properties");) {
      properties.load(input);
    } catch (Exception e) {
      e.printStackTrace();
    }
    this.ownerid = properties.getProperty("ds.ownerid");
    this.orgid = properties.getProperty("ds.orgid");
    this.privkey = properties.getProperty("ds.privatekey");
    this.pubkey = properties.getProperty("ds.publickey");
    this.host = properties.getProperty("host");
    this.uri = properties.getProperty("ds.uri");
    this.connectionstring = properties.getProperty("ds.connectionstring");
    this.projectid = properties.getProperty("ds.projectid");
  }

  public String getPath() {
    return path;
  }

  public String getOwnerId() {
    return this.ownerid;
  }

  public String getOrgId() {
    return this.orgid;
  }

  public String getPrivKey() {
    return this.privkey;
  }

  public String getPublicKey() {
    return this.pubkey;
  }

  public String getHost() {
    return this.host;
  }

  public String getUri() {
    return this.uri;
  }

  public String getConnectionString() {
    return this.connectionstring;
  }

  public String getProjectid() {
    return this.projectid;
  }
}
