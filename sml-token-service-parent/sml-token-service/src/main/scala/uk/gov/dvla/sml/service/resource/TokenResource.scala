package uk.gov.dvla.sml.service.resource

import java.util.Date
import javax.validation.{ConstraintViolation, Validation}
import javax.ws.rs._
import javax.ws.rs.core.{MediaType, Response}

import com.codahale.metrics.annotation.Timed

import uk.gov.dvla.sml.datastore.TokenDatastore
import uk.gov.dvla.sml.domain.LicenceAccessToken
import uk.gov.dvla.sml.service.TokenGenerator
import uk.gov.dvla.sml.service.config.TokenResourceConfig

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

@Path("tokens")
@Consumes(Array(MediaType.APPLICATION_JSON))
@Produces(Array(MediaType.APPLICATION_JSON))
class TokenResource(config: TokenResourceConfig, datastore: TokenDatastore, tokenGenerator: TokenGenerator) {

  private val validator = Validation.buildDefaultValidatorFactory.getValidator

  @POST
  @Timed
  def create(implicit request: ExpirableTokenCreationRequest): Response = withValidRequest { request =>
    val token = generateToken(request.getDriverNumber)
    Try {
      datastore.save(
        LicenceAccessToken.create(request.documentRef, token, request.driverNumber, request.creationDate, request.expiryDate)
      )
    } match {
      case Success(accessToken) =>
        Response.status(Response.Status.CREATED).entity(accessToken).build
      case Failure(e) =>
        Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(SystemFailure(e.getMessage)).build
    }
  }

  @PUT
  @Path("{id}/cancel")
  @Timed
  def cancel(@PathParam("id") id: String): Response = {
    datastore.get(id) match {
      case Some(accessToken) =>
        recordCancellation(accessToken).map { token =>
          Response.status(Response.Status.OK).entity(token).build
        }.getOrElse {
          Response.status(Response.Status.NOT_MODIFIED).build
        }
      case None =>
        Response.status(Response.Status.NOT_FOUND).build
    }
  }

  private def recordCancellation(token: LicenceAccessToken): Option[LicenceAccessToken] = withValidToken(token) {
    val cancelledToken = datastore.cancel(token)
    cancelledToken
  }

  @PUT
  @Path("{id}/redeem")
  @Timed
  def redeem(@PathParam("id") id: String): Response = {
    datastore.get(id) match {
      case Some(accessToken) =>
        recordRedemption(accessToken).map { token =>
          Response.status(Response.Status.OK).entity(token).build
        }.getOrElse {
          Response.status(Response.Status.NOT_MODIFIED).build
        }
      case None =>
        Response.status(Response.Status.NOT_FOUND).build
    }
  }

  private def recordRedemption(token: LicenceAccessToken): Option[LicenceAccessToken] = withValidToken(token) {
    val redeemedToken = datastore.redeem(token)
    redeemedToken
  }

  @GET
  @Path("search")
  @Timed
  def search(@QueryParam("documentRef") documentRef: String,
             @QueryParam("token") token: String): Response = {
    datastore.find(documentRef, token) match {
      case Some(validToken) =>
        Response.status(Response.Status.OK).entity(validToken).build
      case None =>
        Response.status(Response.Status.NOT_FOUND).build
    }
  }

  @GET
  @Path("count/{driverNumber}")
  @Timed
  def countCreated(@PathParam("driverNumber") driverNumber: String, @QueryParam("since") since: Option[Date]): Response = {
    Response.status(Response.Status.OK).entity(
    datastore.findActive(driverNumber, 999).size
    ).build
  }

  @GET
  @Path("search/active/{driverNumber}")
  @Timed
  def searchActive(@PathParam("driverNumber") driverNumber: String): Response = {
    datastore.findActive(driverNumber, config.activeTokensLimit) match {
      case Nil => Response.status(Response.Status.NOT_FOUND).build
      case results => { Response.status(Response.Status.OK).entity(results).build
      }
    }
  }

  @GET
  @Path("search/inactive/{driverNumber}")
  @Timed
  def searchInactive(@PathParam("driverNumber") driverNumber: String): Response = {
    datastore.findInactive(driverNumber, config.inactiveTokensLimit) match {
      case Nil => Response.status(Response.Status.NOT_FOUND).build
      case results => Response.status(Response.Status.OK).entity(results).build
    }
  }

  type ValidationResult = Option[List[ConstraintViolation[Any]]]

  private def validate(o: Any): ValidationResult = validator.validate(o).toList match {
    case Nil => None
    case violations => Some(violations)
  }

  private def withValidRequest(action: ExpirableTokenCreationRequest => Response)(implicit request: ExpirableTokenCreationRequest) =
    validate(request) match {
      case Some(violations) =>
        Response.status(Response.Status.BAD_REQUEST).entity(
          violations.map(ViolationMapper.violationToFailure)
        ).build

      case None => action(request)
    }

  private def withValidToken(token: LicenceAccessToken)(action: => Option[LicenceAccessToken]): Option[LicenceAccessToken] = {
    if (token.isValid) {
      action
    } else {
      None
    }
  }

  private def generateToken(driverNumber: String): String = {

    def generate(existingTokens: List[String]): String = {
      val token = tokenGenerator.generate
      existingTokens.find(_ == token) match {
        case Some(_) => generate(existingTokens)
        case None => token
      }
    }

    generate(
      datastore.findByDriverNumber(driverNumber).map(_.token)
    )
  }

}

object ViolationMapper {

  def violationToFailure(violation: ConstraintViolation[Any]): ValidationFailure =
    new ValidationFailure(violation.getPropertyPath.toString, violation.getMessage)

}
