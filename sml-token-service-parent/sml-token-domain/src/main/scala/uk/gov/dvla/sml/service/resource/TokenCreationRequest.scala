package uk.gov.dvla.sml.service.resource

import javax.validation.constraints.{NotNull, Size}

import org.joda.time.DateTime

import scala.beans.BeanProperty


object TokenCreationRequest {
  def apply(driverNumber: String, documentRef: String): TokenCreationRequest = {
    val request = new TokenCreationRequest
    request.driverNumber = driverNumber
    request.documentRef = documentRef
    request
  }
}

object ExpirableTokenCreationRequest {
  def apply(tokenRequest: TokenCreationRequest, creation: DateTime, expiry: DateTime): ExpirableTokenCreationRequest = {
    val request = new ExpirableTokenCreationRequest
    request.driverNumber = tokenRequest.driverNumber
    request.documentRef = tokenRequest.documentRef
    request.creationDate = creation
    request.expiryDate = expiry
    request
  }
}

class TokenCreationRequest {

  @BeanProperty
  @NotNull
  var driverNumber: String = null

  @BeanProperty
  @NotNull
  @Size(min = 8, max = 8)
  var documentRef: String = null
}

class ExpirableTokenCreationRequest extends TokenCreationRequest {

  @BeanProperty
  @NotNull
  var creationDate: DateTime = null

  @BeanProperty
  @NotNull
  var expiryDate: DateTime = null
}
