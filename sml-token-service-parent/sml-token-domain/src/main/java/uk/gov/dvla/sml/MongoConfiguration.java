package uk.gov.dvla.sml.core;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Default mongo configuration that DVLA-IEP services use.
 * Does contain a list of servers, list of credentials, the database name and mongo collection
 */
public class MongoConfiguration extends BasicMongoConfiguration {

    public MongoConfiguration() {
    }

    public MongoConfiguration(BasicMongoConfiguration config, String collection) {
        this(config.readPreference, config.ensureIndexes, config.servers, config.credentials, config.database, collection);
    }

    public MongoConfiguration(BasicMongoConfiguration.ReadPreference readPreference, boolean ensureIndexes,
                              List<String> servers, List<String> credentials,
                              String database, String collection) {
        this.readPreference = readPreference;
        this.ensureIndexes = ensureIndexes;
        this.servers = servers;
        this.credentials = credentials;
        this.database = database;
        this.collection = collection;
    }

    @NotNull
    private String collection;

    /**
     * @return a mongo collection name
     */
    public final String getCollection() {
        return collection;
    }

}
