package be.cetic.inah.commons

import akka.actor.ActorSystem
import akka.stream.Materializer
import be.cetic.inah.commons.kafka.{KafkaConsumer, KafkaProducer}
import org.apache.kafka.clients.producer.ProducerRecord

object KafkaTest extends App {

  implicit val system = ActorSystem()
  implicit val materializer: Materializer = Materializer(system)

  val kafkaProducer = system.actorOf(KafkaProducer.props(Some("kafka.conf"), "localhost:9092"))
  val kafkaConsumer = new KafkaConsumer(system.deadLetters, "group1", "test", Some("kafka.conf"), "localhost:9092", backPressure = false)

  Seq.range(1, 30).map { i => kafkaProducer ! new ProducerRecord("test", s"testValue$i") }



}
