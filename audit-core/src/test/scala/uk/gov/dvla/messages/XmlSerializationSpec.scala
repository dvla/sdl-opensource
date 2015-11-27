package uk.gov.dvla.messages

import org.joda.time.LocalDateTime
import org.scalatest._
import uk.gov.dvla.auditing.Message

class XmlSerializationSpec extends FlatSpec with Matchers {

  val date = LocalDateTime.now()

  behavior of "Message XML serialization"

  it should "Generate XML" in {
    val message: Message = Message(
      name = "MidAfk",
      serviceType = "LOL",
      "int" -> 1,
      "string" -> "string",
      "date" -> date,
      "list" -> List("foo", "bar").toString,
      "map" -> Map("foo" -> "bar").toString
    )

    message.toXml.toString() should fullyMatch regex s"""<MidAfk list="List\\(foo, bar\\)" messageId="[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}" int="1" date="$date" string="string" map="Map\\(foo -&gt; bar\\)" serviceType="LOL"/>"""
  }

  it should "Generate XML for subclasses" in {
    val message: Message = new TestMessage(date)

    message.toXml.toString() should fullyMatch regex s"""<MidAfk messageId="[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}" int="1" date="$date" string="string" serviceType="LOL"/>"""
  }

  case class TestMessage(date: LocalDateTime) extends Message(
    name = "MidAfk",
    serviceType = "LOL",
    "int" -> 1,
    "string" -> "string",
    "date" -> date)
}
