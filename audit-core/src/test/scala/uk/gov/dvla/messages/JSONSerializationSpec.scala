package uk.gov.dvla.messages

import java.util.UUID

import org.joda.time.LocalDateTime
import org.scalatest._
import uk.gov.dvla.auditing.Message

class JSONSerializationSpec extends FlatSpec with Matchers {

  val date = LocalDateTime.now()

  val messageIdRegex = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
  val message: Message = Message(
    name = "MidAfk",
    serviceType = "LOL",
    "int" -> 1,
    "string" -> "string",
    "date" -> date,
    "missing" -> null,
    "list" -> List("foo", "bar"),
    "object" -> ArbitraryType("bar")
  )

  behavior of "Message Json serialization"

  it should "Generate a nice Json" in {
    val idless = message.toJson.replaceAll(messageIdRegex, "")
    idless should be( s"""{"serviceType":"LOL","name":"MidAfk","string":"string","object":{"foo":"bar"},"date":"$date","int":1,"missing":null,"messageId":"","list":["foo","bar"]}""")
  }

  it should "Generate a nice Json for subclasses" in {
    val idless = TestMessage(date).toJson.replaceAll(messageIdRegex, "")
    idless should be( s"""{"serviceType":"LOL","name":"MidAfk","string":"string","object":{"foo":"bar"},"date":"$date","int":1,"messageId":""}""")
  }

  it should "Go back to class form" in {
    val messageDes = Message.fromJson(
      s"""{"serviceType":"LOL","name":"MidAfk",
         |"string":"string",
         |"date":"$date","int":1,
         |"object":{"foo":"bar"},
         |"list":["foo","bar"],
         |"missing": null,
         |"messageId":"043eb852-b3d8-43e4-a024-d16fde644d32"}""".stripMargin).get
    messageDes.messageId should be(UUID.fromString("043eb852-b3d8-43e4-a024-d16fde644d32"))
    messageDes.name should be("MidAfk")
    messageDes.serviceType should be("LOL")
    messageDes.data.toMap should be(Map(
      "string" -> "string",
      "date" -> date.toString,
      "int" -> "1",
      "missing" -> "null",
      "object" -> Map("foo" -> "bar").toString,
      "list" -> List("foo", "bar").toString
    ))
  }

  case class TestMessage(date: LocalDateTime) extends Message(
    name = "MidAfk",
    serviceType = "LOL",
    "int" -> 1,
    "string" -> "string",
    "date" -> date,
    "object" -> ArbitraryType("bar"))

  case class ArbitraryType(foo: String)
}

