package uk.gov.dvla.sml.datastore

import com.mongodb.{BasicDBObject, DBObject, DBCollection}
import uk.gov.dvla.sml.datastore.TimeToLiveIndex.expireAfterSecondsOption

import scala.collection.JavaConversions._

trait TimeToLiveIndex {
  self: MongoDatastore =>

  val config: MongoDatastoreConfig

  val indexName: String
  val indexedField: String
  val timeToLiveInSeconds: Int

  override def start(): Unit = {
    if (config.mongo.isEnsureIndexes) ensureTimeToLiveIndex()
  }

  private def ensureTimeToLiveIndex(): Unit = {
    def getIndex(collection: DBCollection): Option[DBObject] = {
      collection.getIndexInfo.find(_.get("name").equals(indexName))
    }

    def indexRecreationNeeded(index: DBObject): Boolean = {
      !index.get(expireAfterSecondsOption).equals(timeToLiveInSeconds)
    }

    val collection = mongoClient.getDB(config.mongo.getDatabase).getCollection(config.mongo.getCollection)

    val index = getIndex(collection)

    if (index.isDefined && indexRecreationNeeded(index.get)) {
      collection.dropIndex(indexName)
    }

    collection.createIndex(
      new BasicDBObject(indexedField, 1),
      new BasicDBObject(expireAfterSecondsOption, timeToLiveInSeconds).append("name", indexName)
    )
  }

}

object TimeToLiveIndex {
  val expireAfterSecondsOption = "expireAfterSeconds"
}
