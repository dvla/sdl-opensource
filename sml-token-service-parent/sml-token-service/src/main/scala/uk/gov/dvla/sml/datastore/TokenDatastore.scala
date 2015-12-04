package uk.gov.dvla.sml.datastore

import java.util.{Calendar, Date}

import io.dropwizard.lifecycle.Managed
import uk.gov.dvla.sml.datastore.{MongoDatastore, MongoDatastoreConfig, TimeToLiveIndex}
import uk.gov.dvla.sml.domain.LicenceAccessToken

import scala.collection.JavaConversions._

class TokenDatastore(val config: MongoDatastoreConfig, val timeToLiveInSeconds: Int)
  extends MongoDatastore(config) with TimeToLiveIndex with Managed {

  override def getName = getClass.getSimpleName

  override val indexName: String = TokenDatastore.timeToLiveIndexName
  override val indexedField: String = TokenDatastore.timeToLiveIndexedField
  
  def save(token: LicenceAccessToken): LicenceAccessToken = {
    super.save(token)
    token
  }

  def get(id: String): Option[LicenceAccessToken] = Option(
    datastore.get(classOf[LicenceAccessToken], id)
  )

  def cancel(token: LicenceAccessToken) = {
    val updateOperation = datastore.createUpdateOperations(classOf[LicenceAccessToken]).set("cancelled", new Date)
    datastore.findAndModify(
      datastore.createQuery(classOf[LicenceAccessToken]).field("id").equal(token.id),
      updateOperation
    )
    get(token.id)
  }

  def redeem(token: LicenceAccessToken) = {
    val updateOperation = datastore.createUpdateOperations(classOf[LicenceAccessToken]).set("redeemed", new Date)
    datastore.findAndModify(
      datastore.createQuery(classOf[LicenceAccessToken]).field("id").equal(token.id),
      updateOperation
    )
    get(token.id)
  }

  def find(docRef: String, token: String): Option[LicenceAccessToken] = Option {
    datastore.createQuery(classOf[LicenceAccessToken])
      .field("documentRef").equal(docRef)
      .field("token").equal(token)
      .get
  }

  def findByDriverNumber(driverNumber: String): List[LicenceAccessToken] = {
    datastore.createQuery(classOf[LicenceAccessToken])
      .field("driverNumber").equal(driverNumber)
      .asList().to[List]
  }

  def findActive(driverNumber: String, limit: Int): List[LicenceAccessToken] = {
    datastore.createQuery(classOf[LicenceAccessToken])
      .field("driverNumber").equal(driverNumber)
      .field("expired").greaterThanOrEq(new Date)
      .field("cancelled").equal(null)
      .field("redeemed").equal(null)
      .order("-created")
      .limit(limit)
      .asList().to[List]
  }

  def findInactive(driverNumber: String, limit: Int): List[LicenceAccessToken] = {
    val query = datastore.createQuery(classOf[LicenceAccessToken])
    query.and(query.criteria("driverNumber").equal(driverNumber))
    query.and(query.or(
      List(
        query.criteria("expired").lessThan(new Date),
        query.criteria("cancelled").notEqual(null),
        query.criteria("redeemed").notEqual(null)
      ): _*)
    )
    var documentList = query.asList
    val someList = documentList.sortWith((d1,d2) => getLatestDate(d1).after(getLatestDate(d2))).toList
    someList.take(limit)
  }

  def getLatestDate(licenceToken:LicenceAccessToken): Date = {
    if (licenceToken.expired.after(new Date)) {
       Seq(Option(licenceToken.cancelled), Option(licenceToken.redeemed)).flatten.sortWith((d1,d2) => d1.after(d2)).head
    } else {
       Seq(Option(licenceToken.expired), Option(licenceToken.cancelled), Option(licenceToken.redeemed)).flatten.sortWith((d1,d2) => d1.after(d2)).head
    }
  }

  def countCreated(driverNumber: String, since: Option[Date]): Long = {
    val query = datastore.createQuery(classOf[LicenceAccessToken])

    val criteria = List(query.criteria("driverNumber").equal(driverNumber))
    if (since.isDefined) criteria ++: query.field("created").greaterThanOrEq(since.get)

    query.and(criteria: _*)
    query.countAll()
  }
}

object TokenDatastore {

  val timeToLiveIndexName = "sml_token_ttl"
  val timeToLiveIndexedField = "created"

}
