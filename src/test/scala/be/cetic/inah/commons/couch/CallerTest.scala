package couch

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import be.cetic.inah.commons.dao.couch.{CouchCaller, CouchConnector, Create, CreateSuccess, Delete, DeleteSuccess, Read, ReadAllSuccess, ReadSuccess, Update, UpdateSuccess}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import spray.json._

class CallerTest extends TestKit(ActorSystem("CouchCallerTest")) with ImplicitSender with WordSpecLike
  with Matchers with BeforeAndAfterAll with CouchCaller with DefaultJsonProtocol {

  val client = CouchConnector.connect("test", "localhost", 5984, null, null)

  private val content = Map("hello" -> "world").toJson
  private val content2 = Map("hello" -> "world", "version"->"2").toJson

  val createMessage = Create("k1", content)

  "The couch caller" should {
    "allow C operations" in {
      assert(create(createMessage).getClass == classOf[CreateSuccess])

    }

    "allow R operations" in {
      create(Create("k2", content))
      assert(read(Read("k2")).getClass == classOf[ReadSuccess])
    }

    "allow U operations" in {
      println("")
      create(Create("k2", content))

      assert(update(Update("k2", content2)).getClass == classOf[UpdateSuccess])
    }

    "allow D operations" in {
      create(Create("k3", content))
      assert(delete(Delete("k3")).getClass == classOf[DeleteSuccess])
    }

    "allow reading all" in {
      for(i<-Seq.range(0,100)){
        create(Create(i.toString, content))
      }

      val all = readAll()
      create(Create("k3", content))
      assert(all.getClass == classOf[ReadAllSuccess])
    }
  }

  delete(Delete("k1"))
  delete(Delete("k2"))
  delete(Delete("k3"))
}
