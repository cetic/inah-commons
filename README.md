# Inah Common library

sbt: 
libraryDependencies +=  "be.cetic.inah.commons" % "commons" % "VERSION" from "https://github.com/cetic/inah-commons/raw/master/releases/inah-commons-VERSION.jar"


## Janusgraph

### Connector

````scala
import be.cetic.inah.commons.database.janusgraph.Janusgraph
````

The connecor object implements the following methods: 
```scala
 def setGraphFromConf(conf: String)
 def setGraphCassandraEs(cassandraUrl: String, esUrl: String, batchLoad: Boolean = false, bufferSize: Option[Int] = None): Unit = {
 def setGraphInMemory()
```

The parameters are
- cassandraUrl: Url for cassandra
- esUrl: Url for elastic search 
- batchLoad: connect in batch load mode
- bufferSize: Storage buffer size

- conf: path of the config file

The graph is accessible by
```scala
Janusgraph.graph
```
which is a singleton.

### Schema
Additionally the package 
````scala
be.cetic.inah.commons.database.janusgraph

````

includes a helper class for schema handling and an implementation of the Inah Schema.
```scala
class SchemaMaker(janusgraph: StandardJanusGraph)
class InahSchema(graph: StandardJanusGraph, waitForStatus: Boolean = false, mixed: Boolean = true, reindexation: Boolean = false, enabling: Boolean = true)

```

## CouchDB
### Connector

```scala
import be.cetic.inah.commons.database.couchdb.CouchConnector
```

exposes the following methods:
```scala
 def connect(config: String): CouchDbClient  
 def connect(dbName: String, host: String, port: Int, username: String, password: String, protocol: String = "http", createDbIfNotExist: Boolean = true): CouchDbClient
```
where 

- config points to a valid configuration file

### Utilities

The package provides an actor for CRUD operations 
```scala
CouchActordb(val client: CouchDbClient) 
```
accepting the messages defined in
```scala
be.cetic.inah.commons.database.couchdb.CouchCRUD
``` 

as well as a document actor:
```scala

object CouchDocumentActor {

  case object ReadAllDocuments

  case class ReadDocument(key: String)

  case class CreateDocument[T](document: T)

  case class UpdateDocument[T](document: T)

  case class DeleteDocument(key: String)

  def props[T](couchConfPath: String, couchTable: String)(implicit getKey: T => String, getJson: T => JsValue): Props = Props(new CouchDocumentActor(couchConfPath, couchTable, getKey, getJson))

}


class CouchDocumentActor[T](couchConfPath: String, couchTable: String, getKey: T => String, getJson: T => JsValue)
```


## SQL

Sql is handled with slick, offering a full jdbc support. 
The library must provide the relevant driver. 

The commons library implements a trait:
```scala
import be.cetic.inah.commons.database.sql.DaoFactory
```
```scala
trait DaoFactory {
  val driver: JdbcProfile
  val dbProfile: String = ""
  val schemaName: Option[String] = None
  implicit val dispatcher: ExecutionContextExecutor

  import driver.api._

  val db = Database.forConfig(dbProfile)

  def createSchema(schema: String) = sqlu"""CREATE SCHEMA IF NOT EXISTS "#$schema";"""

  def schemas: driver.DDL

  def createTables = schemas.create

  def dropTables = schemas.drop

  val setupActions = Seq(schemaName.map(createSchema), Some(createTables)).filter(_.isDefined).map(_.get)

  def setup = DBIO.seq(setupActions: _*)

```

## Http

We package the cors support, 
````scala
import be.cetic.inah.commons.http.cors.CorsSupport
````

and a bunch of useful directives in 
````scala
import be.cetic.inah.commons.http.Directives
````
