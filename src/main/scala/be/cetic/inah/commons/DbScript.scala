package be.cetic.inah.commons

import akka.actor.ActorSystem
import be.cetic.inah.commons.database.sql.management.ManagementDaoFactory

import scala.concurrent.ExecutionContextExecutor

object DbScript extends App {

  implicit val system: ActorSystem = ActorSystem("Search")
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
  val managementDao = new ManagementDaoFactory(slick.jdbc.PostgresProfile)

  println(managementDao.schemas.createIfNotExistsStatements.mkString("\n"))

}
