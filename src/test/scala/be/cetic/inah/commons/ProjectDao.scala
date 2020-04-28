package be.cetic.inah.commons


import be.cetic.inah.commons.authentication.PasswordUtil
import be.cetic.inah.commons.database.sql.SqlJsonProtocol
import be.cetic.inah.commons.database.sql.management.ManagementResource
import be.cetic.inah.commons.database.sql.management.model.{PatternDescriptionDto, ServiceOfferDto}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers, WordSpecLike}


class ProjectDaoTest extends WordSpecLike with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with ManagementResource
  with SqlJsonProtocol with PasswordUtil {

  case class Question(questionId: Int, name: String, description: String, options: Seq[String], value: Option[String] = None)
  case class Appendix(descriptionId: Int, name: String, description: String, filename: Option[String])
  case class Subsection(descriptionId: Int, name: String, description: String, content: Option[String])
  case class Section(sectionId: Int, title: String, description: String, content: Seq[Subsection])
  case class Offer(offerId: Int, requiredMultichoice: Seq[Question], requiredAppendices: Seq[Appendix], requiredSections: Seq[Section])
  case class Application(selectedOfferIds: Seq[Int], multichoice: Seq[Question], appendices: Seq[Appendix], sections: Seq[Section])

  "The project dao" should {
    "return the list of possible offers" in {
      def readServiceOffer : Seq[ServiceOfferDto] = ???
    }

    "return the required pattern of an offer" in {
      //Return the object with no value, content or filename.
      def readOfferPattern(offerId: Int) : Offer = ???
    }
  }

  "persist a question content" in {
    case class QuestionContent(projectId: String, questionId: Int, value: String)
    def createOrUpdateQuestionContent(content: QuestionContent): QuestionContent  = ???
  }

  "persist an appendixFile" in {
    case class AppendixContent(projectId: String, descriptionId: Int, filename: String, content: Array[Byte])
    def createOrUpdateAppendixFile(content: AppendixContent): AppendixContent = ???
  }

  "persist a SectionContent" in {
    case class SectionContent(projectId: String, descriptionId: Int, sectionId: Int, content: String )
    def createOrUpdateSectionContent(content: SectionContent) : SectionContent = ???
  }

  "persist offer choice" in {
    def createOrUpdateOfferChoice(projectId: String, offerIds: Seq[Int]) : Seq[Int] = ???
  }

  "read previous project content" in {
      /// Offer *must* contain whatever existing content, including which offer is selected
      // If there is not content, provide the object with None
      //
      def readProjectContent(projectId: String): Application  = ???
  }

}
