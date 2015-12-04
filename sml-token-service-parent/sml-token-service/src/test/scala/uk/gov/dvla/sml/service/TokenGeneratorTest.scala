package uk.gov.dvla.sml.service

import org.apache.commons.lang3.StringUtils
import org.scalatest.{Matchers, FlatSpec}
//import org.slf4j.LoggerFactory

class TokenGeneratorTest extends FlatSpec with Matchers {

  //val log = LoggerFactory.getLogger(getClass)
  val generator = new CommonsTokenGenerator

  behavior of "TokenGenerator"

  it should "generate tokens which are eight characters in length" in {
      generator.generate should have size CommonsTokenGenerator.TOKEN_LENGTH
  }

}
