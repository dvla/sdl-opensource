package uk.gov.dvla.sml.client

import java._
import java.net.URL
import javax.ws.rs.core.MediaType

import com.sun.jersey.api.client.{Client, GenericType}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import uk.gov.dvla.sml.ManagedService
import uk.gov.dvla.sml.client.LicenceAccessTokenClient.serializableDateFormatter
import uk.gov.dvla.sml.domain.LicenceAccessToken
import uk.gov.dvla.sml.service.resource.ExpirableTokenCreationRequest

class LicenceAccessTokenClient(client: Client, endpointUrl: URL, adminEndpointUrl: URL) extends ManagedService {

  def create(request: ExpirableTokenCreationRequest): LicenceAccessToken = {
    client.resource(s"$endpointUrl/tokens")
      .`type`(MediaType.APPLICATION_JSON_TYPE)
      .post(classOf[LicenceAccessToken], request)
  }

  def search(documentRef: String, token: String): LicenceAccessToken = {
    client.resource(s"$endpointUrl/tokens/search")
      .queryParam("documentRef", documentRef)
      .queryParam("token", token)
      .get(classOf[LicenceAccessToken])
  }

  def countCreated(driverNumber: String, since: DateTime): Int = {
    client.resource(s"$endpointUrl/tokens/count/$driverNumber")
      .queryParam("since", since.toString(serializableDateFormatter))
      .get(classOf[Int])
  }

  def searchActive(driverNumber: String): util.List[LicenceAccessToken] = {
    client.resource(s"$endpointUrl/tokens/search/active/$driverNumber")
      .get(new GenericType[util.List[LicenceAccessToken]] {})
  }

  def searchInactive(driverNumber: String): util.List[LicenceAccessToken] = {
    client.resource(s"$endpointUrl/tokens/search/inactive/$driverNumber")
      .get(new GenericType[util.List[LicenceAccessToken]] {})
  }

  def cancel(tokenID: String): LicenceAccessToken = {
    client.resource(s"$endpointUrl/tokens/$tokenID/cancel")
      .put(classOf[LicenceAccessToken])
  }

  def redeem(tokenID: String): LicenceAccessToken = {
    client.resource(s"$endpointUrl/tokens/$tokenID/redeem")
      .put(classOf[LicenceAccessToken])
  }

  override def getName: String = "licence-access-token-client"

  override def start(): Unit = {}

  override def stop(): Unit = client.destroy()

  override def isAlive: Boolean = {
    client.resource(s"$adminEndpointUrl/healthcheck").head.getStatus == 200
  }
}

object LicenceAccessTokenClient {
  val serializableDateFormatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zzz yyyy").withZoneUTC()
}
