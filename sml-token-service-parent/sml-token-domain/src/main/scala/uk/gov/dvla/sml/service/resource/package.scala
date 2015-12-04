package uk.gov.dvla.sml.service

package object resource {

  case class ValidationFailure(fieldPath: String, msg: String)

  case class SystemFailure(msg: String)

}
