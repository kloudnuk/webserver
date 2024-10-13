package com.kloudnuk.webserver.services;

import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Sorts.orderBy;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.kloudnuk.webserver.services.api.IDataStoreProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.bson.Document;
import org.bson.conversions.Bson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

@Service("mongoProvider")
public class MongoDbSimpleProvider implements IDataStoreProvider {

  private static final Logger log = LoggerFactory.getLogger(MongoDbSimpleProvider.class);
  private ApplicationProps aProps = new ApplicationProps();
  private String conn = aProps.getConnectionString();
  private MongoClient mongo = MongoClients.create(conn);

  @Override
  public MongoDatabase getDatabase(String orgname) {
    return mongo.getDatabase(orgname);
  }

  @Override
  public MongoCollection<Document> getCollection(MongoDatabase db, String name) {
    return db.getCollection(name);
  }

  @Override
  public CompletableFuture<List<Document>> getDocumentsAsJson(MongoCollection<Document> collection,
      int limit) {

    CompletableFuture<List<Document>> future = CompletableFuture.supplyAsync(() -> {
      if (limit == 0) {
        List<Document> list = new ArrayList<Document>(limit);
        collection.find(Filters.empty()).limit(limit).allowDiskUse(true).forEach(d -> list.add(d));
        return list;
      } else {
        List<Document> list = new ArrayList<Document>();
        collection.find(Filters.empty()).allowDiskUse(true).forEach(d -> list.add(d));
        return list;
      }
    });
    return future;
  }

  @Override
  public CompletableFuture<List<Document>> getDeviceData(String orgname, String[] properties) {

    CompletableFuture<List<Document>> future = CompletableFuture.supplyAsync(() -> {
      List<Document> list = new ArrayList<Document>();
      try {
        MongoCollection<Document> devices = getCollection(getDatabase(orgname), "Devices");
        Bson select;
        if (properties.length <= 0) {
          select = fields(exclude("_id"),
              include(new String[] {"properties.device-name.value", "id", "address", "last synced",
                  "properties.application-software-version", "properties.database-revision.value",
                  "properties.description.value", "properties.firmware-revision.value",
                  "properties.last-restore-reason.value", "properties.location.value",
                  "properties.model-name.value", "properties.protocol-revision.value",
                  "properties.system-status.value", "properties.time-of-device-restart.value",
                  "properties.vendor-name.value"}));
        } else {
          select = fields(exclude("_id"), include(properties));
        }
        devices.find(Filters.empty()).allowDiskUse(true).projection(select)
            .sort(ascending("properties.device-name.value")).forEach(doc -> list.add(doc));
      } catch (Exception e) {
        log.error(e.getLocalizedMessage(), e);
      }
      return list;
    });
    return future;
  }

  @Override
  public CompletableFuture<List<Document>> getPointLists(String orgname) {

    CompletableFuture<List<Document>> future = CompletableFuture.supplyAsync(() -> {
      try {
        MongoCollection<Document> pointLists = getCollection(getDatabase(orgname), "Points");
        List<Document> list = new ArrayList<Document>();
        Bson select = exclude("_id");
        pointLists.find(Filters.empty()).allowDiskUse(true).projection(select)
            .sort(orderBy(descending("id"), ascending("name"))).forEach(d -> list.add(d));
        return list;
      } catch (Exception e) {
        log.error(e.getLocalizedMessage(), e);
      }
      return new ArrayList<Document>();
    });
    return future;
  }

  @Override
  public CompletableFuture<List<Document>> getDeviceConfigurations(String orgname) {

    CompletableFuture<List<Document>> future = CompletableFuture.supplyAsync(() -> {
      try {
        MongoCollection<Document> configs = getCollection(getDatabase(orgname), "Configuration");
        List<Document> list = new ArrayList<Document>();
        Bson select = exclude("_id");
        configs.find(Filters.empty()).allowDiskUse(true).projection(select)
            .sort(orderBy(ascending("device.label"), ascending("device.nukid")))
            .forEach(d -> list.add(d));
        return list;
      } catch (Exception e) {
        log.error(e.getLocalizedMessage(), e);
      }
      return new ArrayList<Document>();
    });
    return future;
  }

  @Override
  public CompletableFuture<List<Document>> getDeviceLogs(String orgname) {
    CompletableFuture<List<Document>> future = CompletableFuture.supplyAsync(() -> {
      try {
        Bson select = exclude("_id");
        Bson where = Filters.empty();
        MongoCollection<Document> logs = getCollection(getDatabase(orgname), "Logs");
        List<Document> list = new ArrayList<Document>();
        logs.find(where).allowDiskUse(true).projection(select).sort(descending("timestamp"))
            .forEach(d -> {
              list.add(d);
            });
        return list;
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
      return new ArrayList<Document>();
    });
    return future;
  }

  @Override
  public CompletableFuture<List<Document>> getDeviceLogsByRegex(String orgname, String regex) {
    CompletableFuture<List<Document>> future = CompletableFuture.supplyAsync(() -> {
      try {
        Bson select = exclude("_id");
        Bson where = Filters.regex("timestamp", regex);
        MongoCollection<Document> logs = getCollection(getDatabase(orgname), "Logs");
        List<Document> list = new ArrayList<Document>();
        logs.find(where).allowDiskUse(true).projection(select).sort(descending("timestamp"))
            .forEach(d -> {
              list.add(d);
            });
        return list;
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
      return new ArrayList<Document>();
    });
    return future;
  }
}
