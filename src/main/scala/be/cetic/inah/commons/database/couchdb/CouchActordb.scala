package be.cetic.inah.commons.database.couchdb

import akka.actor.{Actor, Props}
import com.typesafe.config.Config
import org.lightcouch.CouchDbClient

object CouchActordb {

  def props(dbName: String, host: String, port: Int, username: String, password: String, protocol: String = "http",
            createDbIfNotExist: Boolean = true): Props = {
    val client = new CouchDbClient(dbName, createDbIfNotExist, protocol, host, port, username, password)
    Props(new CouchActordb(client))
  }

  def props(config: Config): Props = {
    val dbName = config.getString("name")
    val host = config.getString("host")
    val port = config.getInt("port")
    val username = if (config.hasPath("username")) config.getString("username") else null
    val password = if (config.hasPath("password")) config.getString("password") else null
    val protocol = config.getString("protocol")
    val createDbIfNotExist = config.getBoolean("createdb.if-not-exist")

    props(dbName, host, port, username, password, protocol, createDbIfNotExist)

  }

  def props(config: String): Props = {
    val client = new CouchDbClient(config)
    Props(new CouchActordb(client))
  }
}

class CouchActordb(val client: CouchDbClient) extends Actor with CouchCaller {

  def receive: Receive = {
    case crudMessage: CouchCRUD => sender ! crud(crudMessage)
    case ReadAll => sender ! readAll()
    case _ => ???
  }


}