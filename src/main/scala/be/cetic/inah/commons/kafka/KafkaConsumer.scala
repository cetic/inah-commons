package be.cetic.inah.commons.kafka

import akka.actor.{ActorRef, ActorSystem}
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import be.cetic.inah.commons.kafka.KafkaConsumer.{StreamComplete, StreamError, StreamInit}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer

object KafkaConsumer {
  case object Ack
  case object StreamInit
  case object StreamComplete
  case class StreamError(e: Throwable)
}

class KafkaConsumer(forwardTo: ActorRef, groupId: String, topic: String, configPath: Option[String], bootstrapServer: String, backPressure: Boolean)(implicit system: ActorSystem,  val materializer: Materializer) {

  val config = configPath.map(ConfigFactory.load).getOrElse(ConfigFactory.load).getConfig("akka.kafka.producer")
  val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers(bootstrapServer)
    .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
    .withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000")
    .withGroupId(groupId)

  val consumerSource: Source[ConsumerRecord[String, String], Consumer.Control] = Consumer.plainSource(consumerSettings, Subscriptions.topics(topic))

  val kafkaSink = if(backPressure) Sink.actorRefWithBackpressure[ConsumerRecord[String, String]](forwardTo, StreamInit, KafkaConsumer.Ack, StreamComplete, e=> StreamError(e))
  else Sink.actorRef[ConsumerRecord[String, String]](forwardTo, StreamComplete, (t: Throwable) => t)

  def run()= consumerSource.to(kafkaSink).run

}
