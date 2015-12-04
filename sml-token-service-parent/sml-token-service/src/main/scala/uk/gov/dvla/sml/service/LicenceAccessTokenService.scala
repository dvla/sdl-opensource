package uk.gov.dvla.sml.service

import com.massrelevance.dropwizard.ScalaApplication
import com.massrelevance.dropwizard.bundles.ScalaBundle
import io.dropwizard.setup.{Bootstrap, Environment}
import uk.gov.dvla.sml.datastore.MongoDatastoreConfig

import uk.gov.dvla.sml.datastore.TokenDatastore
import uk.gov.dvla.sml.service.config.LicenceAccessTokenServiceConfig
import uk.gov.dvla.sml.service.resource.TokenResource

class LicenceAccessTokenApplication extends ScalaApplication[LicenceAccessTokenServiceConfig] {

  override def initialize(bootstrap: Bootstrap[LicenceAccessTokenServiceConfig]): Unit = {
    bootstrap.addBundle(new ScalaBundle)
  }

  override def run(config: LicenceAccessTokenServiceConfig, env: Environment): Unit = {
    val mongoDatastoreConfig = new MongoDatastoreConfig(config.mongo, Some("uk.gov.dvla.sml.domain"))
    val tokenDatastore = new TokenDatastore(mongoDatastoreConfig, config.mongo.tokenTTLSeconds)
    env.lifecycle().manage(tokenDatastore)
    env.jersey.register(new TokenResource(config.resource, tokenDatastore, new CommonsTokenGenerator))
  }

}

object LicenceAccessTokenService extends LicenceAccessTokenApplication
