package be.cetic.inah.commons.database.dao

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import akka.util.Timeout
import be.cetic.inah.commons.database.dao.ProjectDaoActor._
import be.cetic.inah.commons.database.sql.SqlDao
import be.cetic.inah.commons.database.sql.management.model._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}


object ProjectDaoActor {

  case class ReadOfferPattern(offerId: Int)

  case object ReadServiceOffers

  case class SaveQuestionContent(content: QuestionContentDto)

  case class SaveAppendixContent(content: AppendixContentDto)

  case class SaveSubsectionContent(content: SubsectionContentDto)

  case class SaveOfferChoice(projectOfferDto: ProjectOfferDto)

  case class ReadOfferContent(projectId: String)

  def props: Props = Props(new ProjectDaoActor())
}

class ProjectDaoActor extends Actor with ActorLogging with ContentReader {

  implicit val dispatcher: ExecutionContextExecutor = context.dispatcher
  val sqlDao = new SqlDao(slick.jdbc.PostgresProfile)
  val dao = sqlDao.managementDao

  import dao.driver.api._

  implicit val timeout: Timeout = Timeout(5 seconds)

  def receive: Receive = {
    case ReadServiceOffers => readServiceOffer pipeTo sender
    case ReadOfferPattern(offerId) => readOfferPattern(offerId) pipeTo sender
    case SaveQuestionContent(content) => createOrUpdateQuestionContent(content) pipeTo sender
    case SaveAppendixContent(content) => createOrUpdateAppendixFile(content) pipeTo sender
    case SaveSubsectionContent(content) => createOrUpdatesubsectionContent(content) pipeTo sender
    case SaveOfferChoice(projectOfferDto) => createOrUpdateOfferChoice(projectOfferDto) pipeTo sender
    case ReadOfferContent(projectId) => readApplication(projectId) pipeTo sender
    case msg => log.error(s"$this received unhandled $msg.")
  }

  def readServiceOffer: Future[Seq[ServiceOfferDto]] = dao.db.run(dao.ServiceOfferDao.readAll)

  def createOrUpdateOfferChoice(projectOffer: ProjectOfferDto): Future[ProjectOfferDto] = {
    val q = dao.ProjectOfferDao.projectOffers.insertOrUpdate(projectOffer).map(_ => projectOffer)
    dao.db.run(q)
  }

  def createOrUpdateQuestionContent(content: QuestionContentDto): Future[QuestionContentDto] = {
    val q = dao.QuestionContentDao.questionContents.insertOrUpdate(content).map(_ => content)
    dao.db.run(q)
  }

  def createOrUpdateAppendixFile(content: AppendixContentDto): Future[AppendixContentDto] = {
    val q = dao.AppendixContentDao.appendixContents.insertOrUpdate(content)
    dao.db.run(q).map(_ => content)
  }

  def createOrUpdatesubsectionContent(content: SubsectionContentDto): Future[SubsectionContentDto] = {
    val q = dao.SubsectionContentDao.subsectionContents.insertOrUpdate(content).map(_ => content)
    dao.db.run(q)
  }
}