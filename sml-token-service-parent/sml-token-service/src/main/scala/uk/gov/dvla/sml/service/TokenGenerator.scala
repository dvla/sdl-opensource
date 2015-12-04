package uk.gov.dvla.sml.service

import org.apache.commons.lang3.RandomStringUtils

trait TokenGenerator {

  def generate: String

}

class CommonsTokenGenerator extends TokenGenerator {

  private val numerals = "23456789"

  override def generate: String = RandomStringUtils.random(CommonsTokenGenerator.TOKEN_LENGTH, numerals)

}

object CommonsTokenGenerator {

  val TOKEN_LENGTH = 8

}
