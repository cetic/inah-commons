package be.cetic.inah.commons.kafka

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer

object KafkaConsumer {
  def props(forwardTo: ActorRef, groupId: String, topic: String, configPath: Option[String], bootstrapServer: String)(implicit materializer: Materializer) = Props(new KafkaConsumer(forwardTo, groupId, topic, configPath, bootstrapServer))
}

class KafkaConsumer(forwardTo: ActorRef, groupId: String, topic: String, configPath: Option[String], bootstrapServer: String)(implicit val materializer: Materializer) extends Actor with ActorLogging {

  val config = configPath.map(ConfigFactory.load).getOrElse(ConfigFactory.load).getConfig("akka.kafka.producer")
  val consumerSettings = ConsumerSettings(context.system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers(bootstrapServer)
    .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
    .withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000")
    .withGroupId(groupId)

  val consumerSource: Source[ConsumerRecord[String, String], Consumer.Control] = Consumer.plainSource(consumerSettings, Subscriptions.topics(topic))

  val kafkaSink = Sink.actorRef[ConsumerRecord[String, String]](self, "completed", (t: Throwable) => t)

  consumerSource.to(kafkaSink).run

  def receive = {
    case "completed" => log.info(s"$this : stream completed, topic: $topic")

    case t: Throwable => log.error(t.getMessage)

    case c: ConsumerRecord[String, String] =>
      log.info(s"$this: ${c.toString}")
      forwardTo ! c
  }
}
