package be.cetic.inah.commons.database.sql

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging, Props}
import slick.jdbc.JdbcBackend.DatabaseDef

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import akka.pattern.pipe
import slick.jdbc.JdbcProfile

object SqlDaoActor {

  case object ReadAll

  case class Read[Id](id: Id)

  case class Delete[Id](id: Id)

  case class Create[T](o: T)

  case class Update[T](o: T)

  def props[T, Id](dao: Dao[T, Id]) = Props(new SqlDaoActor(dao))
}

class SqlDaoActor[T, Id](dao: Dao[T, Id], extraReceive: Receive = PartialFunction.empty[Any, Unit])
  extends Actor with ActorLogging {

  implicit val dispatcher: ExecutionContextExecutor = context.dispatcher
  val db = SqlDao.dao.db

  override def receive: Receive = {
    case SqlDaoActor.ReadAll => db.run(dao.readAll) pipeTo sender
    case SqlDaoActor.Read(id: Id) => db.run(dao.read(id)) pipeTo sender
    case SqlDaoActor.Delete(id: Id) => db.run(dao.delete(id)) pipeTo sender
    case SqlDaoActor.Create(o: T) => db.run(dao.create(o)) pipeTo sender
    case SqlDaoActor.Update(o: T) => db.run(dao.update(o)) pipeTo sender
  }

  override def unhandled(message: Any): Unit = {
    case msg => log.error(s"$this received unhandled $msg.")
  }
}
