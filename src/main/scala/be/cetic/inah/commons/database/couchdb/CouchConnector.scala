package be.cetic.inah.commons.database.couchdb

import org.lightcouch.CouchDbClient

object CouchConnector {

  def connect(config: String): CouchDbClient = {
    new CouchDbClient(config)
  }

  def connect(dbName: String, host: String, port: Int, username: String, password: String, protocol: String = "http",
              createDbIfNotExist: Boolean = true): CouchDbClient = {
    new CouchDbClient(dbName, createDbIfNotExist, protocol, host, port, username, password)

  }

  def close(client: CouchDbClient): Unit = {
    client.close()
  }
}
