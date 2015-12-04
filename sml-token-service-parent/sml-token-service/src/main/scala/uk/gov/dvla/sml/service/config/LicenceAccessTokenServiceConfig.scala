package uk.gov.dvla.sml.service.config

import javax.validation.Valid
import javax.validation.constraints.NotNull

import io.dropwizard.Configuration

class LicenceAccessTokenServiceConfig extends Configuration {

  @NotNull @Valid
  var mongo: TokenMongoConfig = _

  @Valid
  var resource: TokenResourceConfig = new TokenResourceConfig

}
