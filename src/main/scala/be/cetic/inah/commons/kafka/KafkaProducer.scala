package be.cetic.inah.commons.kafka

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.actor.Status.Success
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import akka.stream.{CompletionStrategy, Materializer, OverflowStrategy}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer


object KafkaProducer{
  def props(configPath: Option[String], bootstrapServer: String)(implicit materializer: Materializer ) = Props(new KafkaProducer(configPath, bootstrapServer))
}

class KafkaProducer(configPath: Option[String], bootstrapServer: String)(implicit val materializer: Materializer ) extends Actor with ActorLogging{

  val config = configPath.map(ConfigFactory.load).getOrElse(ConfigFactory.load).getConfig("akka.kafka.producer")
  val producerSettings = ProducerSettings(context.system, new StringSerializer, new StringSerializer).withBootstrapServers(bootstrapServer)

  val completionMatcher: PartialFunction[Any, CompletionStrategy] = {case _: Success => CompletionStrategy.immediately}

  val source = Source.actorRefWithBackpressure[ProducerRecord[String, String]]("ack", completionMatcher, PartialFunction.empty)
  val kafkaActor = source.to(Producer.plainSink(producerSettings)).run

  var ready = true

  var messages= Seq[ProducerRecord[String, String]]()

  override def receive: Receive = {

    case rc: ProducerRecord[String, String] =>
      messages = messages :+  rc
      consumeQueue()

    case "ack" =>
      ready=true
      consumeQueue()
  }

  def consumeQueue()= {
    if (ready && messages.nonEmpty) {
      val message = messages.head
      messages = messages.drop(1)
      ready = false
      kafkaActor ! message
    }
  }

}
