package be.cetic.inah.commons


import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.TestKit
import akka.pattern.ask
import akka.util.Timeout
import be.cetic.inah.commons.authentication.PasswordUtil
import be.cetic.inah.commons.database.dao.ProjectDaoActor
import be.cetic.inah.commons.database.model.{Application, Offer}
import be.cetic.inah.commons.database.sql.SqlJsonProtocol
import be.cetic.inah.commons.database.sql.management.ManagementResource
import be.cetic.inah.commons.database.sql.management.model.{AppendixContentDto, PatternDescriptionDto, ProjectOfferDto, QuestionContentDto, ServiceOfferDto, SubsectionContentDto}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers, WordSpecLike}

import scala.concurrent.duration._

class ProjectDaoActorTest extends TestKit(ActorSystem("ProjectDaoSystem")) with WordSpecLike with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with ManagementResource
  with SqlJsonProtocol with PasswordUtil {


  implicit val timeout = Timeout(10 seconds)
  val projectDaoActor: ActorRef = system.actorOf(ProjectDaoActor.props)

  val offerId = ???
  val questionContentDto : QuestionContentDto= ???
  val subsectionContent: SubsectionContentDto = ???
  val appendixDto : AppendixContentDto = ???
  val offerChoice : ProjectOfferDto = ???

  "The project dao" should {
    "return the list of possible offers" in {
      projectDaoActor ? ProjectDaoActor.ReadServiceOffers
      expectMsgType[Seq[ServiceOfferDto]]
    }

    "return the required pattern of an offer" in {
      //Return the object with no value, content or filename.
      projectDaoActor ? ProjectDaoActor.ReadOfferPattern(offerId)
      expectMsgType[Offer]
    }
  }

  "persist a question content" in {
    projectDaoActor ? questionContentDto
    expectMsg(questionContentDto)
  }

  "persist an appendixFile" in {
    projectDaoActor ? appendixDto
    expectMsg(appendixDto)
    }

  "persist a SubsectionContent" in {
    projectDaoActor ? subsectionContent
    expectMsg(subsectionContent)
  }


  "persist offer choice" in {
    projectDaoActor ? offerChoice
    expectMsg(offerChoice)
  }

  "read previous project content" in {
    /// Offer *must* contain whatever existing content, including which offer is selected
    // If there is not content, provide the object with None
    //
    def readProjectContent(projectId: String): Application = ???
  }

}
