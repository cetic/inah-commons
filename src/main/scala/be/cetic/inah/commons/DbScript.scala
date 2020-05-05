package be.cetic.inah.commons

import akka.actor.ActorSystem
import be.cetic.inah.commons.database.sql.SqlDao

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}

object DbScript extends App {

  implicit val system: ActorSystem = ActorSystem("DbDpl")
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
  val sqlDao = new SqlDao(slick.jdbc.PostgresProfile)

  val setup = for {
    _ <- sqlDao.db.run(sqlDao.driver.api.DBIO.sequence(sqlDao.createSchemaStatements)).recoverWith{case t:Throwable => Future(Nil)   }
    _ <- sqlDao.db.run(sqlDao.setup)
  } yield {}
  Await.result(setup, Duration.Inf)
  system.terminate()
}

object DbScriptPrint extends App {

  implicit val system: ActorSystem = ActorSystem("DbDpl")
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
  val sqlDao = new SqlDao(slick.jdbc.PostgresProfile)

  println("")
  import sqlDao.managementDao.driver.api._
  sqlDao.managementDao.schemas.create.statements.map(println)
  println("")
  system.terminate()
}

