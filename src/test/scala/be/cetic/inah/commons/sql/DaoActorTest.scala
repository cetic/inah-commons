package be.cetic.inah.commons.sql

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.testkit.TestKit
import akka.util.Timeout
import be.cetic.inah.commons.authentication.PasswordUtil
import be.cetic.inah.commons.database.sql.management.model.ProjectDto
import be.cetic.inah.commons.database.sql.{SqlDao, SqlDaoActor, SqlJsonProtocol}
import be.cetic.inah.commons.database.sql.management.{ManagementResource, SqlManagementJsonProtocol}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers, WordSpecLike}
import akka.pattern.ask
import spray.json.DefaultJsonProtocol

import scala.concurrent.duration._
import scala.concurrent.ExecutionContextExecutor

class DaoActorTest extends TestKit(ActorSystem("DaoTest")) with WordSpecLike with Matchers with BeforeAndAfterEach with BeforeAndAfterAll with
  DefaultJsonProtocol with SqlJsonProtocol with ManagementResource with SqlManagementJsonProtocol with PasswordUtil {


  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(5.seconds)

  var routes: Route = _
  val setup= new SetupSqlTest()
  val daoActor = SqlDaoActor.props(setup.sqlDao.managementDao.ProjectDao)
  override def beforeAll() = {
    setup.setup()
  }

  "The project Dao actor should" {
    "create a project" in {
      daoActor ? SqlDaoActor.Create(ProjectDto(None, "test"))
    }
  }

}
