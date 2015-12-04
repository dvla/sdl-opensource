package uk.gov.dvla.sml.datastore

import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterAll, Suite}
import uk.gov.dvla.sml.core.BasicMongoConfiguration.ReadPreference
import uk.gov.dvla.sml.datastore.{MongoDatastore, MongoDatastoreConfig}
import uk.gov.dvla.sml.domain.LicenceAccessToken

trait TokenDatastoreSpec extends Suite with BeforeAndAfterAll {

  var datastore: TokenDatastore = _

  val datastoreConfig = MongoDatastoreConfig("localhost" :: Nil, "test", "tokens", Nil, ReadPreference.PRIMARY, ensureIndexes = true)
  val defaultTokenTTLSeconds = 4

  val now = DateTime.now()
  val after72Hours = now.plusHours(72)

  override protected def beforeAll(): Unit = {
    dropCollection()
    datastore = new TokenDatastore(datastoreConfig, defaultTokenTTLSeconds)
  }

  override protected def afterAll(): Unit = {
    dropCollection()
  }

  private def dropCollection() =
    MongoDatastore.createDatastore(datastoreConfig).getCollection(classOf[LicenceAccessToken]).drop()
}
