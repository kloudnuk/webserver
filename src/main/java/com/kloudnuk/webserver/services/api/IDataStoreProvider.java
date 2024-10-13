package com.kloudnuk.webserver.services.api;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.util.concurrent.CompletableFuture;
import java.util.List;

public interface IDataStoreProvider {

    public MongoDatabase getDatabase(String orgname);

    public MongoCollection<Document> getCollection(MongoDatabase db, String name);

    public CompletableFuture<List<Document>> getDocumentsAsJson(
            MongoCollection<Document> collection, int limit);

    public CompletableFuture<List<Document>> getDeviceData(String orgname,
            String[] userSelectedProperties);

    public CompletableFuture<List<Document>> getPointLists(String orgname);

    public CompletableFuture<List<Document>> getDeviceConfigurations(String orgname);

    public CompletableFuture<List<Document>> getDeviceLogs(String orgname);

    public CompletableFuture<List<Document>> getDeviceLogsByRegex(String orgname, String regex)
            throws Exception;
}
