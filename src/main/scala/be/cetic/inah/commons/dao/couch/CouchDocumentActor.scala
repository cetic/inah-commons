package be.cetic.inah.commons.dao.couch

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import be.cetic.inah.commons.dao.couch.CouchDocumentActor._
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import spray.json._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._


object CouchDocumentActor {

  case object ReadAllDocuments

  case class ReadDocument(key: String)

  case class CreateDocument[T](document: T)

  case class UpdateDocument[T](document: T)

  case class DeleteDocument(key: String)

  def props[T](couchConfPath: String, couchTable: String)(implicit getKey: T => String, getJson: T => JsValue): Props = Props(new CouchDocumentActor(couchConfPath, couchTable, getKey, getJson))

}


class CouchDocumentActor[T](couchConfPath: String, couchTable: String, getKey: T => String, getJson: T => JsValue) extends Actor with ActorLogging  {

  implicit val timeout: Timeout = Timeout(10 seconds)
  implicit val dispatcher: ExecutionContextExecutor = context.dispatcher

  private val conf = ConfigFactory.load(couchConfPath)
  private val couchConf = conf.getConfig("couchdb")
    .withValue("name", ConfigValueFactory.fromAnyRef(couchTable))

  private val dbActor = context.actorOf(CouchActordb.props(couchConf))

  def receive: Receive = {
    case ReadAllDocuments => (dbActor ? ReadAll).mapTo[ReadAllResponse] pipeTo sender
    case CreateDocument(doc: T) => dbActor ? Create(getKey(doc), getJson(doc)) pipeTo sender
    case ReadDocument(key) => (dbActor ? Read(key)).mapTo[ReadResponse] pipeTo sender
    case UpdateDocument(doc: T) => (dbActor ? Update(getKey(doc), getJson(doc))).mapTo[UpdateResponse] pipeTo sender
    case DeleteDocument(key) => dbActor ? Delete(key) pipeTo sender
  }

}



