package uk.gov.dvla.sml.core;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic mongo configuration that DVLA-IEP services use.
 * Does contain a list of servers, list of credentials and the database name
 */
public class BasicMongoConfiguration {

    protected ReadPreference readPreference = ReadPreference.NEAREST;

    protected boolean ensureIndexes = false;

    @NotNull
    protected List<String> servers = new ArrayList<>();

    protected List<String> credentials = new ArrayList<>();

    @NotNull
    protected String database;

    /**
     * @return a read preference which will be used by morphia, defaults to 'ReadPreference.NEAREST'
     */
    public com.mongodb.ReadPreference getReadPreference() {
        if (readPreference == null) {
            return com.mongodb.ReadPreference.nearest();
        }
        switch (readPreference) {
            case PRIMARY:
                return com.mongodb.ReadPreference.primary();

            case PRIMARY_PREFERRED:
                return com.mongodb.ReadPreference.primaryPreferred();

            case SECONDARY:
                return com.mongodb.ReadPreference.secondary();

            case SECONDARY_PREFERRED:
                return com.mongodb.ReadPreference.secondaryPreferred();

            case NEAREST:
                return com.mongodb.ReadPreference.nearest();

            default:
                return com.mongodb.ReadPreference.nearest();
        }
    }

    /**
     * @return a flag if the database indexes should be ensured. Set true only for services writing to the database, defaults to 'false'
     */
    public boolean isEnsureIndexes() {
        return ensureIndexes;
    }

    /**
     * @return a list of servers in host:port format which is used to connect with the replica sets
     */
    public List<String> getServers() {
        return servers;
    }

    /**
     * @return a list of credentials in login:pass format which is used to connect with the replica sets
     */
    public List<String> getCredentials() {
        return credentials;
    }

    /**
     * @return a the database name
     */
    public final String getDatabase() {
        return database;
    }

    public enum ReadPreference {
        PRIMARY, PRIMARY_PREFERRED, SECONDARY, SECONDARY_PREFERRED, NEAREST
    }

}
