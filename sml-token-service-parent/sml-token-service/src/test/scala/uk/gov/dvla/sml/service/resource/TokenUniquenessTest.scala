package uk.gov.dvla.sml.service.resource

import java.util.Date
import javax.ws.rs.core.Response
import javax.ws.rs.{QueryParam, PathParam, Path, GET}

import com.codahale.metrics.annotation.Timed
import org.joda.time.DateTime
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.{Matchers, FlatSpec}
import uk.gov.dvla.sml.datastore.TokenDatastore
import uk.gov.dvla.sml.domain.LicenceAccessToken
import uk.gov.dvla.sml.service.TokenGenerator
import uk.gov.dvla.sml.service.config.TokenResourceConfig

class TokenUniquenessTest extends FlatSpec with Matchers {

  val driverNumber = "KITSO802192GT2BD"
  val token1 = "lDVWfnUA"
  val token2 = "Fx489unq"

  val request = TokenCreationRequest(driverNumber, "192GT2BD")
  implicit def token2ExpToken(token: TokenCreationRequest): ExpirableTokenCreationRequest = ExpirableTokenCreationRequest(token, DateTime.now(), DateTime.now().plusHours(72))

  behavior of "TokenResource.create"

  it should "re-generate a token when generated value matches existing tokens" in {
    val datastore = createDatastore { mockedDatastore =>
      when(mockedDatastore.findByDriverNumber(driverNumber)).thenReturn(List(
        createAccessToken(token1), createAccessToken("cDEW2nPZ")
      ))
      when(mockedDatastore.save(any[LicenceAccessToken]))
        .thenReturn(createAccessToken(token2))
    }
    val generator = createGenerator
    val resource = createResource(datastore, generator)

    val accessToken = resource.create(request).getEntity.asInstanceOf[LicenceAccessToken]

    verify(generator, times(2)).generate
    accessToken.token should be (token2)
  }


  it should "return the number of created tokens for a driver number" in {
    val datastore = createDatastore { mockedDatastore =>
      when(mockedDatastore.findActive(driverNumber,999)).thenReturn(List(
        createAccessToken(token1), createAccessToken(token2)
      ))
    }
    val generator = createGenerator
    val resource = createResource(datastore, generator)

    val number = resource.countCreated(driverNumber, None).getEntity().asInstanceOf[Integer]
    number should be (2)
  }


  it should "use the first generated value when it doesn't match existing tokens" in {
    val datastore = createDatastore { mockedDatastore =>
      when(mockedDatastore.findByDriverNumber(driverNumber)).thenReturn(List(
        createAccessToken("aBc123dE"), createAccessToken("cDEW2nPZ")
      ))
      when(mockedDatastore.save(any[LicenceAccessToken]))
        .thenReturn(createAccessToken(token1))
    }
    val generator = createGenerator
    val resource = createResource(datastore, generator)

    val accessToken = resource.create(request).getEntity.asInstanceOf[LicenceAccessToken]
    verify(generator, times(1)).generate
    accessToken.token should be (token1)
  }

  it should "use the first generated value when there are no existing tokens" in {
    val datastore = createDatastore { mockedDatastore =>
      when(mockedDatastore.findByDriverNumber(driverNumber)).thenReturn(List.empty)
      when(mockedDatastore.save(any[LicenceAccessToken]))
        .thenReturn(createAccessToken(token1))
    }
    val generator = createGenerator
    val resource = createResource(datastore, generator)

    val accessToken = resource.create(request).getEntity.asInstanceOf[LicenceAccessToken]

    verify(generator, times(1)).generate
    accessToken.token should be (token1)
  }

  private def createAccessToken(token: String) =
    LicenceAccessToken.create("192GT2BD", token, driverNumber, DateTime.now, DateTime.now.plusDays(1))

  private def createGenerator: TokenGenerator = {
    val mockedGenerator = mock(classOf[TokenGenerator])
    when(mockedGenerator.generate).thenReturn(token1).thenReturn(token2)
    mockedGenerator
  }

  private def createDatastore(stubbingBehavior: TokenDatastore => Unit): TokenDatastore = {
    val mockedDatastore = mock(classOf[TokenDatastore])
    stubbingBehavior(mockedDatastore)
    mockedDatastore
  }

  private def createResource(datastore: TokenDatastore, generator: TokenGenerator): TokenResource =
    new TokenResource(new TokenResourceConfig, datastore, generator)

}
