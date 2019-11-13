package be.cetic.inah.commons.couch

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import be.cetic.inah.commons.database.couchdb.{CouchDocumentActor, CreateSuccess, DeleteSuccess, ReadSuccess}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import spray.json.{DefaultJsonProtocol, JsValue, RootJsonFormat}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor}


class CouchDocumentFactoryTest extends TestKit(ActorSystem("DocumentFactoryTest")) with ImplicitSender with WordSpecLike
  with Matchers with BeforeAndAfterAll with DefaultJsonProtocol {

  implicit val timeout = Timeout(20 seconds)
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher


  case class TestDocument(name: String, content: String)

  implicit val testJsonFormat: RootJsonFormat[TestDocument] = jsonFormat2(TestDocument)

  implicit def getKey(d: TestDocument): String = d.name

  implicit def getJson(d: TestDocument): JsValue = d.toJson

  val testDoc = TestDocument("foo", "bar")
  val testDoc2 = TestDocument("bar", "baz")
  val testDoc3 = TestDocument("baz", "foz")
  val testDoc4 = TestDocument("foz", "buz")

  val confPath = "couchdb.template.properties"
  val documentActor: ActorRef = system.actorOf(CouchDocumentActor.props[TestDocument](confPath, "test-table"))

  "Document Factory Actor" should {
    "write a document" in {
      val doc = Await.result(documentActor ? CouchDocumentActor.CreateDocument(testDoc), 2 seconds)

      assert(doc.getClass == classOf[CreateSuccess])

    }

    "read api authorization" in {
      val doc = Await.result(documentActor ? CouchDocumentActor.CreateDocument(testDoc2), 2 seconds)

      val read = CouchDocumentActor.ReadDocument(getKey(testDoc2))
      val readDoc = Await.result(documentActor ? read, 2 seconds).asInstanceOf[ReadSuccess]
      val res = readDoc.content.convertTo[TestDocument]
      assert(res == testDoc2)
    }

    "delete api authorization" in {

      val doc = Await.result(documentActor ? CouchDocumentActor.CreateDocument(testDoc3), 2 seconds)

      val del = Await.result(documentActor ? CouchDocumentActor.DeleteDocument(testDoc3.name), 2 seconds)

      assert(del.getClass == classOf[DeleteSuccess])

    }
  }

  documentActor ? CouchDocumentActor.DeleteDocument(testDoc.name)

  documentActor ? CouchDocumentActor.DeleteDocument(testDoc2.name)
}


class DocumentFactory {

}
