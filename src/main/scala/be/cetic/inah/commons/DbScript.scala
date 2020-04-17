package be.cetic.inah.commons

import akka.actor.ActorSystem
import be.cetic.inah.commons.database.sql.access_control.model.TokenDto
import be.cetic.inah.commons.database.sql.{SchemaNames, SqlDao}
import be.cetic.inah.commons.database.sql.management.ManagementDaoFactory

import scala.concurrent.ExecutionContextExecutor

object DbScript extends App {

  implicit val system: ActorSystem = ActorSystem("Search")
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
  val sqlDao = new SqlDao(slick.jdbc.PostgresProfile)
  SchemaNames.setNoGivenSchema
  println(sqlDao.createSchemaStatements.map(_.statements.head).mkString("\n"))
  println(sqlDao.schemas.createIfNotExistsStatements.mkString("\n"))

  system.terminate()
}
