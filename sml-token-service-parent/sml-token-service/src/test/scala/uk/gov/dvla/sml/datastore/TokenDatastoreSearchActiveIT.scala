package uk.gov.dvla.sml.datastore

import org.scalatest.{FlatSpec, Matchers}
import uk.gov.dvla.sml.domain.LicenceAccessToken

class TokenDatastoreSearchActiveIT extends FlatSpec with Matchers with TokenDatastoreSpec {

  val driverNumber = "KITSO802192GT2BD"

  val tokenA = LicenceAccessToken("192GT2BD", "Ab3Gj459", driverNumber, now.minusMillis(2).toDate, after72Hours.toDate, null, null)
  val tokenB = LicenceAccessToken("254CP3YA", "Bb3Gj989", driverNumber, now.minusMillis(1).toDate, after72Hours.toDate, null, null)

  behavior of "Token Datastore"

  it should "order active tokens by date descending" in {
    val result: List[LicenceAccessToken] = datastore.findActive(driverNumber, 999)

    result.length should be(2)
    result.head.token should be(tokenB.token)
    result.last.token should be(tokenA.token)
  }

  it should "limit result set according to provided value" in {
    val result: List[LicenceAccessToken] = datastore.findActive(driverNumber, 1)

    result.length should be(1)
    result.head.token should be(tokenB.token)
  }

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    datastore.save(tokenA)
    datastore.save(tokenB)
  }

}