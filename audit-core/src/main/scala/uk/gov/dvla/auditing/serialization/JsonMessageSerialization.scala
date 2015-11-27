package uk.gov.dvla.auditing.serialization

import uk.gov.dvla.auditing.Message

/**
 * Simple trait for JSON serialization.
 */
trait JsonMessageSerialization extends JsonSerializable {
  self: Message =>

  /**
   * Serializes this object to an JSON representation.
   *
   * @return JSON nodes representing this class.
   */
  def toJson: String = {
    val base = Map("name" -> name, "serviceType" -> serviceType, "messageId" -> messageId)
    jsonFromMap(data.toSeq.toMap ++ base)
  }

  private def jsonFromMap(data: Map[String, Any]): String = {
    JsonUtils.toJson(data)
  }
}