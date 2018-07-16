package com.comd.creditnote.api.infrastructure.persistence.mongodb;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.io.IOException;
import java.util.Properties;

@ApplicationScoped
public class MongoDbConfigurator {

    private Properties mongoDbProperties;

    @PostConstruct
    private void initProperties() {
        try {
        	mongoDbProperties = new Properties();
        	mongoDbProperties.load(MongoDbConfigurator.class.getResourceAsStream("/mongodb.properties"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @MONGODB
    @Produces 
    public Properties exposeMongoDbProperties() throws IOException {
        final Properties properties = new Properties();
        properties.putAll(mongoDbProperties);
        return properties;
    }

}
