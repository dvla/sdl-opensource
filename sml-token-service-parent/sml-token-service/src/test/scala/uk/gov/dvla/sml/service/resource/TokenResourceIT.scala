package uk.gov.dvla.sml.service.resource

import java.util.Date
import javax.ws.rs.core.{MediaType, Response}

import com.sun.jersey.api.client.{GenericType, ClientResponse}
import org.joda.time.DateTime
import org.mongodb.morphia.{Datastore, Key}
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}
import uk.gov.dvla.core.BasicMongoConfiguration.ReadPreference
import uk.gov.dvla.datastore.{MongoDatastore, MongoDatastoreConfig}
import uk.gov.dvla.service.testing.dropwizard.DropwizardSpec
import uk.gov.dvla.sml.client.LicenceAccessTokenClient
import uk.gov.dvla.sml.domain.LicenceAccessToken
import uk.gov.dvla.sml.service.LicenceAccessTokenApplication
import uk.gov.dvla.sml.service.config.LicenceAccessTokenServiceConfig
import uk.gov.dvla.testing.matchers.DateMatchers

class TokenResourceIT extends FlatSpec with Matchers with DateMatchers with DropwizardSpec[LicenceAccessTokenServiceConfig] {

  val application = new LicenceAccessTokenApplication
  val configPath = getClass.getResource("/config.yaml").getFile

  val now = DateTime.now()
  val after72Hours = now.plusHours(72)

  val createdToken = LicenceAccessToken("192GT2BD", "Ab3Gj889", "KITSO802192GT2BD", now.toDate, null, null, null)
  val kitsonToken = LicenceAccessToken("192GT2BD", "Ab3Gj459", "KITSO802192GT2BD", now.toDate, after72Hours.toDate, null, null)
  val cundillToken = LicenceAccessToken("254CP3YA", "zb3Gj989", "CUNDI80180254CP3YA", now.toDate, after72Hours.toDate, null, null)
  val sommersToken = LicenceAccessToken("254CP3YA", "zb3Gj989", "SOMME52951107385BI", now.toDate, after72Hours.toDate, null, null)
  val expiredToken = LicenceAccessToken("0254CP3YA", "zb3rj989", "KITSO802192GT2BD", now.minusDays(6).toDate, now.minusDays(3).toDate, null, null)
  var cancelledToken = LicenceAccessToken("936ED0FT", "pw55HT2w", "KITSO802192GT2BD", now.toDate, after72Hours.toDate, new Date, null)
  var redeemedToken = LicenceAccessToken("219CP3SZ", "db6Gj555", "KITSO802192GT2BD", now.toDate, after72Hours.toDate, null, new Date)

  var datastore: Datastore = _

  behavior of "TokenResource"

  it should "create a new token when passed a valid request" in {
    val driverNumber = "AAAPY602123BH9PH"
    val documentRef = "219CP3SZ"

    val response = httpClient.resource("http://localhost:6666/tokens")
      .`type`(MediaType.APPLICATION_JSON)
      .post(classOf[ClientResponse], ExpirableTokenCreationRequest(
      TokenCreationRequest(driverNumber, documentRef),
      now,
      after72Hours
    ))

    response.getStatus should be(Response.Status.CREATED.getStatusCode)
    val entity = response.getEntity(classOf[LicenceAccessToken])
    entity.driverNumber should be(driverNumber)
    entity.documentRef should be(documentRef)
    entity.token should not be null
    entity.id should not be null
    entity.isRedeemed should be(false)
  }

  it should "reject a new token request with missing values" in {
    val response = httpClient.resource("http://localhost:6666/tokens")
      .`type`(MediaType.APPLICATION_JSON)
      .post(classOf[ClientResponse], TokenCreationRequest(null, null))
    response.getStatus should be(Response.Status.BAD_REQUEST.getStatusCode)
  }

  it should "reject a new token request with invalid values" in {
    val response = httpClient.resource("http://localhost:6666/tokens")
      .`type`(MediaType.APPLICATION_JSON)
      .post(classOf[ClientResponse], TokenCreationRequest("AB12345678910XJX", "123456789"))
    response.getStatus should be(Response.Status.BAD_REQUEST.getStatusCode)

    response.getEntity(classOf[Array[ValidationFailure]]) should not be empty
  }

  it should "find a valid token" in {
    val response = httpClient.resource("http://localhost:6666/tokens/search")
      .queryParam("documentRef", kitsonToken.documentRef)
      .queryParam("token", kitsonToken.token)
      .get(classOf[ClientResponse])

    response.getStatus should be(Response.Status.OK.getStatusCode)
    val entity = response.getEntity(classOf[LicenceAccessToken])
    entity.driverNumber should be(kitsonToken.driverNumber)
    entity.hasExpired should be(false)
    entity.isRedeemed should be(false)
  }

  it should "find an expired token" in {
    val response = httpClient.resource("http://localhost:6666/tokens/search")
      .queryParam("documentRef", expiredToken.documentRef)
      .queryParam("token", expiredToken.token)
      .get(classOf[ClientResponse])

    response.getStatus should be(Response.Status.OK.getStatusCode)
    val entity = response.getEntity(classOf[LicenceAccessToken])
    entity.driverNumber should be(expiredToken.driverNumber)
    entity.hasExpired should be(true)
  }

  it should "find a redeemed token" in {
    val response = httpClient.resource("http://localhost:6666/tokens/search")
      .queryParam("documentRef", redeemedToken.documentRef)
      .queryParam("token", redeemedToken.token)
      .get(classOf[ClientResponse])

    response.getStatus should be(Response.Status.OK.getStatusCode)
    val entity = response.getEntity(classOf[LicenceAccessToken])
    entity.driverNumber should be(redeemedToken.driverNumber)
    entity.isRedeemed should be(true)
  }

  it should "return not found when asked to find a token with unknown values" in {
    val response = httpClient.resource("http://localhost:6666/tokens/search")
      .queryParam("documentRef", "non_existent")
      .queryParam("token", "non_existent")
      .get(classOf[ClientResponse])

    response.getStatus should be(Response.Status.NOT_FOUND.getStatusCode)
  }

  it should "find only valid tokens when searching for active tokens" in {
    val response = httpClient.resource(s"http://localhost:6666/tokens/search/active/${kitsonToken.driverNumber}")
      .get(classOf[ClientResponse])

    response.getStatus should be(Response.Status.OK.getStatusCode)
    val entity = response.getEntity(new GenericType[List[LicenceAccessToken]] {})

    entity.length should be (1)
    entity.head.driverNumber should be(kitsonToken.driverNumber)
    entity.head.hasExpired should be(false)
    entity.head.isRedeemed should be(false)
  }

  it should "return not found when searching for active tokens and no active tokens have been found" in {
    val response = httpClient.resource("http://localhost:6666/tokens/search/active/ABCDEFG12345678")
      .get(classOf[ClientResponse])

    response.getStatus should be(Response.Status.NOT_FOUND.getStatusCode)
    response.hasEntity should be (false)
  }

  it should "return 0 when counting and can't find any tokens" in {
    val response = httpClient.resource(s"http://localhost:6666/tokens/count/ABCDEFG12345678")
      .get(classOf[ClientResponse])

    response.getStatus should be(Response.Status.OK.getStatusCode)

    val entity = response.getEntity(classOf[Long])

    entity should be (0)
  }

  it should "find expired, cancelled and redeemed tokens when searching for inactive tokens" in {
    val response = httpClient.resource(s"http://localhost:6666/tokens/search/inactive/${kitsonToken.driverNumber}")
      .get(classOf[ClientResponse])

    response.getStatus should be(Response.Status.OK.getStatusCode)
    val entity = response.getEntity(new GenericType[List[LicenceAccessToken]] {})

    entity.length should be (3)
    entity(0).driverNumber should be(cancelledToken.driverNumber)
    entity(1).driverNumber should be(redeemedToken.driverNumber)
    entity(2).driverNumber should be(expiredToken.driverNumber)
  }

  it should "return not found when searching for inactive tokens and no inactive tokens have been found" in {
    val response = httpClient.resource("http://localhost:6666/tokens/search/inactive/ABCDEFG12345678")
      .get(classOf[ClientResponse])

    response.getStatus should be(Response.Status.NOT_FOUND.getStatusCode)
    response.hasEntity should be (false)
  }

  it should "mark a token as cancelled" in {
    val response = httpClient.resource(s"http://localhost:6666/tokens/${cundillToken.getId}/cancel")
      .put(classOf[ClientResponse])

    response.getStatus should be(Response.Status.OK.getStatusCode)
    val entity = response.getEntity(classOf[LicenceAccessToken])
    entity.cancelled should beBeforeNow
    entity.isCancelled should be(true)
  }

  it should "not cancel a token which has already been cancelled" in {
    val response = httpClient.resource(s"http://localhost:6666/tokens/${cancelledToken.getId}/cancel")
      .put(classOf[ClientResponse])

    response.getStatus should be(Response.Status.NOT_MODIFIED.getStatusCode)
  }

  it should "not cancel a token which has already been redeemed" in {
    val response = httpClient.resource(s"http://localhost:6666/tokens/${redeemedToken.getId}/cancel")
      .put(classOf[ClientResponse])

    response.getStatus should be(Response.Status.NOT_MODIFIED.getStatusCode)
  }

  it should "not cancel a token which has already expired" in {
    val response = httpClient.resource(s"http://localhost:6666/tokens/${expiredToken.getId}/cancel")
      .put(classOf[ClientResponse])

    response.getStatus should be(Response.Status.NOT_MODIFIED.getStatusCode)
  }

  it should "return not found when asked to cancel a token which doesn't exist" in {
    val response = httpClient.resource(s"http://localhost:6666/tokens/123/cancel")
      .put(classOf[ClientResponse])

    response.getStatus should be(Response.Status.NOT_FOUND.getStatusCode)
  }

  it should "mark a token as redeemed" in {
    val response = httpClient.resource(s"http://localhost:6666/tokens/${sommersToken.getId}/redeem")
      .put(classOf[ClientResponse])

    response.getStatus should be(Response.Status.OK.getStatusCode)
    val entity = response.getEntity(classOf[LicenceAccessToken])
    entity.redeemed should beBeforeNow
    entity.isRedeemed should be(true)
  }

  it should "not redeem a token which has already been cancelled" in {
    val response = httpClient.resource(s"http://localhost:6666/tokens/${cancelledToken.getId}/redeem")
      .put(classOf[ClientResponse])

    response.getStatus should be(Response.Status.NOT_MODIFIED.getStatusCode)
  }

  it should "not redeem a token which has already been redeemed" in {
    val response = httpClient.resource(s"http://localhost:6666/tokens/${redeemedToken.getId}/redeem")
      .put(classOf[ClientResponse])

    response.getStatus should be(Response.Status.NOT_MODIFIED.getStatusCode)
  }

  it should "not redeem a token which has already expired" in {
    val response = httpClient.resource(s"http://localhost:6666/tokens/${expiredToken.getId}/redeem")
      .put(classOf[ClientResponse])

    response.getStatus should be(Response.Status.NOT_MODIFIED.getStatusCode)
  }

  it should "return not found when asked to redeem a token which doesn't exist" in {
    val response = httpClient.resource(s"http://localhost:6666/tokens/123/redeem")
      .put(classOf[ClientResponse])

    response.getStatus should be(Response.Status.NOT_FOUND.getStatusCode)
  }

  override protected def beforeAll(): Unit = {
    val db = "dvla_test"
    System.setProperty("dw.mongo.database", db)
    super.beforeAll()
    datastore = MongoDatastore.createDatastore(
      MongoDatastoreConfig("localhost" :: Nil, db, "tokens", Nil, ReadPreference.PRIMARY)
    )
    dropCollection()
    save(kitsonToken)
    save(cundillToken)
    save(sommersToken)
    save(expiredToken)
    save(cancelledToken)
    save(redeemedToken)
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    dropCollection()
  }

  private def save(token: LicenceAccessToken): Key[LicenceAccessToken] =
    datastore.save(token)

  private def dropCollection() =
    datastore.getCollection(classOf[LicenceAccessToken]).drop()

}
