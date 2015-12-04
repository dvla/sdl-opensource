package uk.gov.dvla.sml.datastore

import uk.gov.dvla.sml.core.BasicMongoConfiguration.ReadPreference
import uk.gov.dvla.sml.core.MongoConfiguration

import scala.collection.JavaConversions._

object MongoDatastoreConfig {

  def apply(servers: List[String],
            database: String,
            collection: String,
            credentials: List[String],
            readPreference: ReadPreference,
            ensureIndexes: Boolean = false,
            domainPackage: Option[String] = None) = {
    new MongoDatastoreConfig(
      new MongoConfiguration(readPreference, ensureIndexes, servers, credentials, database, collection),
      domainPackage
    )
  }

}

class MongoDatastoreConfig(val mongo: MongoConfiguration = null, val domainPackage: Option[String] = None)
