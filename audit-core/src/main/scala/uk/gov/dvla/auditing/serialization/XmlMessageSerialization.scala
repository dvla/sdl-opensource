package uk.gov.dvla.auditing.serialization

import uk.gov.dvla.auditing.Message

import scala.xml._

/**
 * Simple trait for XML serialization.
 */
trait XmlMessageSerialization extends XmlSerializable {
  self: Message =>
  /**
   * Serializes this object to an XML representation.
   *
   * @return XML nodes representing this class.
   */
  def toXml: NodeSeq = {
    val base = Map("serviceType" -> serviceType, "messageId" -> messageId)
    xmlFromMap(data.toSeq.toMap ++ base)
  }

  private def xmlFromMap(data: Map[String, Any]): NodeSeq = {
    val xmlTag = <a/>.copy(label = name)

    (xmlTag /: data) {
      case (rec, (key, value)) =>
        rec % Attribute(None, key, Text(value.toString), Null)
    }
  }
}