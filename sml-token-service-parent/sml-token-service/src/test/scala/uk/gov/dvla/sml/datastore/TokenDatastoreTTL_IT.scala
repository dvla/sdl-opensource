package uk.gov.dvla.sml.datastore

import com.mongodb.DBObject
import org.scalatest.{FlatSpec, Matchers}
import uk.gov.dvla.sml.core.BasicMongoConfiguration.ReadPreference
import uk.gov.dvla.sml.datastore.{TimeToLiveIndex, MongoDatastore, MongoDatastoreConfig}
import uk.gov.dvla.sml.domain.LicenceAccessToken

import scala.collection.JavaConversions._

class TokenDatastoreTTL_IT extends FlatSpec with Matchers with TokenDatastoreSpec {

  behavior of "Token Datastore"

  it should "automatically delete a token after the configured time to live has passed" in {
    datastore.start()
    val savedToken = datastore.save(
      LicenceAccessToken(
        "192GT2BD", "Ab3Gj459", "KITSO802192GT2BD",
        now.toDate, after72Hours.toDate, null, null)
    )
    datastore.get(savedToken.getId) should be ('defined)
    /*
      Wait just over a minute for Mongo TTL process, which runs every 60 seconds, to delete document.
      Quote from http://docs.mongodb.org/manual/tutorial/expire-data :
      "The background task that removes expired documents runs every 60 seconds.
      As a result, documents may remain in a collection after they expire
      but before the background task runs or completes."
     */
    Thread.sleep(70000)
    datastore.get(savedToken.getId) should be (None)
  }
  
  it should "drop and create index when tokenTTLSeconds config has changed and index recreation allowed" in {
    val indexBeforehand = getTimeToLiveIndex
    indexBeforehand.get.get(TimeToLiveIndex.expireAfterSecondsOption) should be (defaultTokenTTLSeconds)

    val ttlChange = 6
    new TokenDatastore(datastoreConfig, ttlChange).start()

    val indexAfterwards = getTimeToLiveIndex
    indexAfterwards should be ('defined)
    indexAfterwards.get.get(TimeToLiveIndex.expireAfterSecondsOption) should be (ttlChange)
  }

  it should "ignore tokenTTLSeconds config change when index recreation is disallowed" in {
    new TokenDatastore(datastoreConfig, defaultTokenTTLSeconds).start()
    val indexBeforehand = getTimeToLiveIndex
    indexBeforehand.get.get(TimeToLiveIndex.expireAfterSecondsOption) should be (defaultTokenTTLSeconds)

    val revisedDatastoreConfig = MongoDatastoreConfig("localhost" :: Nil, "dvla_test", "tokens", Nil, ReadPreference.PRIMARY, ensureIndexes = false)
    new TokenDatastore(revisedDatastoreConfig, timeToLiveInSeconds = 6).start()
    val indexAfterwards = getTimeToLiveIndex
    indexAfterwards.get.get(TimeToLiveIndex.expireAfterSecondsOption) should be (defaultTokenTTLSeconds)
  }

  private def getTimeToLiveIndex: Option[DBObject] = {
    MongoDatastore.createClient(datastoreConfig)
      .getDB(datastoreConfig.mongo.getDatabase)
      .getCollection(datastoreConfig.mongo.getCollection)
      .getIndexInfo.find(_.get("name").equals(TokenDatastore.timeToLiveIndexName))
  }

}
