package com.comd.creditnote.api.infrastructure.persistence.mongodb;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;

@ApplicationScoped
public class MongoDbConfigurator {

    private Properties mongoDbProperties;

    private static final Logger logger = Logger.getLogger(MongoDbConfigurator.class.getName());

    @PostConstruct
    private void initProperties() {
        logger.log(Level.INFO, "Initializing MongoDb properties...");

        try {
            mongoDbProperties = new Properties();
            mongoDbProperties.load(MongoDbConfigurator.class.getResourceAsStream("/mongodb.properties"));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading 'mongodb.properties file': {0}", e.getMessage());
            throw new IllegalStateException(e);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Unexpected exception: {0}", ex);
            throw new IllegalStateException(ex);
        }

        logger.log(Level.INFO, "MongoDb properties initialized: {0}", mongoDbProperties);
    }

    @MONGODB
    @Produces
    @RequestScoped
    public Properties exposeMongoDbProperties() throws IOException {
        final Properties properties = new Properties();
        properties.putAll(mongoDbProperties);
        return properties;
    }

}
