package uk.gov.dvla.iep.testing.rabbitmq

import java.text.SimpleDateFormat
import java.util.concurrent.LinkedBlockingQueue

import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client._
import org.codehaus.jackson.map.ObjectMapper

import scala.collection.JavaConversions._
import scala.collection.mutable

object QueueUtil {

  private val rabbitmqHost = "localhost"
  private val rabbitmqUser = "guest"
  private val rabbitmqPass = "guest"

  def apply(exchange: String, queue: String, binding: String): QueueUtil =
    new QueueUtil(QueueConfig(exchange, queue, binding) :: Nil)

  def apply(queues: QueueConfig*) = new QueueUtil(queues)
}

class QueueUtil(queueConfigs: Seq[QueueConfig]) {

  val connection = makeConnection
  val channel = initChannel
  private val objectMapper = getMapper

  def msgCount(queue: String): Int =
    channel.queueDeclarePassive(queue).getMessageCount

  def consumer(queue: String): QueueingConsumer = {
    val consumer = new QueueingConsumer(channel)
    channel.basicConsume(queue, false, consumer)
    consumer
  }

  /**
   * Send using Jackson Object Mapper to produce message bytes
   */
  def send(exchange: String, msg: AnyRef, headers: mutable.Map[String, AnyRef] = mutable.Map.empty) {
    val bytes = objectMapper.writeValueAsBytes(msg)
    _send(exchange, bytes, headers)
  }

  /**
   * Send String with no object mapping
   */
  def sendString(exchange: String, msg: String, headers: mutable.Map[String, AnyRef] = mutable.Map.empty) {
    _send(exchange, msg.getBytes, headers)
  }

  /**
   * Receive String with no object mapping
   */
  def receiveString(queue: String): Option[String] = {
    val response = channel.basicGet(queue, true)
    if (response == null) None
    else Some(new String(response.getBody))
  }

  /**
   * Receive using Jackson Object Mapper
   */
  def receive[T](queue: String, `type`: Class[T]): Option[T] = {
    val response = channel.basicGet(queue, true)
    if (response == null) None
    else Some(objectMapper.readValue(response.getBody, `type`))
  }

  /**
   * Receive multiple using Jackson Object Mapper
   */
  def receive[T](queue: String, `type`: Class[T], timeoutInMillis: Int): List[T] = {
    val startTime: Long = System.currentTimeMillis
    var receivedList = List[T]()
    do {
      val received = receive(queue, `type`)
      if (received.isDefined) {
        receivedList :+= received.get
      }
    } while (System.currentTimeMillis - startTime < timeoutInMillis)
    receivedList
  }

  def acknowledge(deliveryTag: Long): Unit = {
    channel.basicAck(deliveryTag, false)
  }

  def purge(queue: String) {
    channel.queuePurge(queue)
  }

  def close() {
    channel.close()
    connection.close()
  }

  private def makeConnection: Connection = {
    val factory = new ConnectionFactory
    factory.setHost(QueueUtil.rabbitmqHost)
    factory.setUsername(QueueUtil.rabbitmqPass)
    factory.setPassword(QueueUtil.rabbitmqUser)
    factory.newConnection
  }

  private def initChannel: Channel = {
    val channel = connection.createChannel
    queueConfigs.foreach { qc =>
      channel.exchangeDeclare(qc.exchange, "direct", true)
      channel.queueDeclare(qc.name, true, false, false, null)
      channel.queueBind(qc.name, qc.exchange, qc.binding)
    }
    channel
  }

  private def config(exchangeName: String): Option[QueueConfig] =
    queueConfigs.find(_.exchange == exchangeName)

  private def getMapper: ObjectMapper = {
    val mapper: ObjectMapper = new ObjectMapper
    mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"))
    mapper
  }

  private def _send(exchange: String, bytes: Array[Byte], headers: mutable.Map[String, AnyRef] = mutable.Map.empty): Unit = {
    val props = new BasicProperties().builder()
    if (headers.nonEmpty) props.headers(headers)
    config(exchange).foreach { qc =>
      channel.basicPublish(qc.exchange, qc.binding, props.build, bytes)
    }
  }

}

case class QueueConfig(exchange: String, name: String, binding: String)

case class Delivery(consumerTag: String, envelope: Envelope, properties: BasicProperties, body: Array[Byte])

class QueueingConsumer(channel: Channel) extends DefaultConsumer(channel) {

  private val queue = new LinkedBlockingQueue[Delivery]()

  override def handleDelivery(consumerTag: String, envelope: Envelope, properties: BasicProperties, body: Array[Byte]): Unit = {
    queue.put(Delivery(consumerTag, envelope, properties, body))
  }

  def nextDelivery: Delivery = {
    queue.take()
  }

}