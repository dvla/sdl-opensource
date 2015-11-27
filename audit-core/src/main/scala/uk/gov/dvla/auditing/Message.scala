package uk.gov.dvla.auditing

import java.util.{Objects, UUID}

import uk.gov.dvla.auditing.serialization.{JsonUtils, JsonMessageSerialization, XmlMessageSerialization}

import scala.util.Try

/**
 * The basic interface which represents something which can be queued onto a Bus.
 */
class Message(val name: String, val serviceType: String, val data: (String, Any)*) extends XmlMessageSerialization with JsonMessageSerialization {
  /**
   * Unique message identifier.
   */
  var messageId = UUID.randomUUID

  def getDataAsJava = {
    import scala.collection.JavaConverters._
    data.toMap.asJava
  }

  def getData[T >: Null](key: String): T = {
    data.find(_._1 == key).map(_._2.asInstanceOf[T]).orNull
  }

  override def toString: String = {
    val dataStr = data.map {
      case (k, v) => s"$k:$v"
    }.mkString(",")

    s"Message($name,$serviceType,$messageId,$dataStr)"
  }
}

object Message {

  def apply(name: String, serviceType: String, data: (String, Any)*): Message = {
    new Message(name, serviceType, data: _*)
  }

  def create(messageName: String, serviceType: String, data: java.util.Map[String, Any]): Message = {
    import scala.collection.JavaConverters._
    Message(messageName, serviceType, data.asScala.toSeq: _*)
  }

  def fromJson(json: String): Try[Message] = Try {
    val mapped = JsonUtils.toMap[Any](json).mapValues(Objects.toString(_))
    val coreKeys = List("name", "serviceType", "messageId")
    val data = mapped.filterKeys(!coreKeys.contains(_))

    val msg = Message(mapped("name"), mapped("serviceType"), data.toSeq: _*)
    msg.messageId = UUID.fromString(mapped("messageId"))
    msg
  }
}