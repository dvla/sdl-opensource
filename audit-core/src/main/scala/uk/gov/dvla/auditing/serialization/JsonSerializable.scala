package uk.gov.dvla.auditing.serialization


trait JsonSerializable {
  def toJson: String
}
