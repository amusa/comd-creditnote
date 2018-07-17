package com.comd.creditnote.api.infrastructure.persistence.mongodb;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;

@ApplicationScoped
public class MongoDbDataSource {

    private MongoDatabase mongoDb;
    private static final Logger logger = Logger.getLogger(MongoDbDataSource.class.getName());

    @MONGODB
    @Inject
    Properties mongoDbProperties;

    @PostConstruct
    private void initProperties() {
        logger.log(Level.INFO, "Initializing Datasource...");
       
        String dbURIString = mongoDbProperties.getProperty("mongodb.url");
        String db = mongoDbProperties.getProperty("mongodb.db");
        MongoClient mongoClient = new MongoClient(new MongoClientURI(dbURIString));
        mongoDb = mongoClient.getDatabase(db);
        
        logger.log(Level.INFO, "Datasource initialized");
    }

    @Produces
    @RequestScoped
    public MongoDatabase exposeMongoDbSource() throws IOException {
        return mongoDb;
    }

}
