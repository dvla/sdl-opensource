package uk.gov.dvla.sml.datastore

import java.util.Date

import org.scalatest.{FlatSpec, Matchers}
import uk.gov.dvla.sml.domain.LicenceAccessToken

class TokenDatastoreSearchInactiveIT extends FlatSpec with Matchers with TokenDatastoreSpec {

  val driverNumber = "KITSO802192GT2BD"

  val tokenA = LicenceAccessToken("192GT2BD", "Ab3Gj459", driverNumber, now.toDate, null, null, null)
  val tokenB = LicenceAccessToken("254CP3YA", "Bb3Gj989", driverNumber, now.plusDays(1).toDate, now.toDate, null, now.plusDays(6).toDate)
  val tokenC = LicenceAccessToken("254CP3BA", "Bb3Gj979", driverNumber, now.plusDays(2).toDate, now.plusMinutes(1).toDate, now.toDate, now.toDate)
  val tokenD = LicenceAccessToken("254CP3CA", "Bb3Gj969", driverNumber, now.plusDays(3).toDate, now.toDate, now.plusDays(6).plusMillis(1).toDate, now.toDate)
  val tokenE = LicenceAccessToken("254CP3DA", "Bb3Gj959", driverNumber, now.plusDays(4).toDate, now.toDate, null, now.plusMinutes(6).toDate)
  val tokenF = LicenceAccessToken("254CP3EA", "Bb3Gj949", driverNumber, now.plusDays(5).toDate, now.toDate, now.plusMinutes(7).toDate, now.toDate)

  behavior of "Token Datastore"

  it should "order active tokens by date descending" in {
    var result: List[LicenceAccessToken] = datastore.findInactive(driverNumber, 999)

    result.length should be(5)
    result.head.token should be(tokenD.token)
    result.last.token should be(tokenC.token)
  }

  it should "limit result set according to provided value" in {
    val result: List[LicenceAccessToken] = datastore.findInactive(driverNumber, 1)

    result.length should be(1)
    result.head.token should be(tokenD.token)
  }

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    datastore.save(tokenA)
    datastore.save(tokenB)
    datastore.save(tokenC)
    datastore.save(tokenD)
    datastore.save(tokenE)
    datastore.save(tokenF)
  }

}