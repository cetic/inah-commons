package be.cetic.inah.commons.database.sql.management

import be.cetic.inah.commons.database.sql.DaoFactory
import be.cetic.inah.commons.database.sql.access_control.model.TokensDtoMultiDb
import be.cetic.inah.commons.database.sql.management.model._
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContextExecutor

class ManagementDaoFactory(val driver: JdbcProfile, val dbProfile: Option[String] = None)(implicit val dispatcher: ExecutionContextExecutor)
  extends DaoFactory with TokensDtoMultiDb with DatasourcesDtoMultiDb with ProjectsDtoMultiDb with ProjectDatasourcesDtoMultiDb
    with ProjectResourcesDtoMultiDb with FeedbackDtoMultiDb
    with ResourcesDtoMultiDb with UsersDtoMultiDb with ProjectUsersDtoMultiDb with ServiceOffersDtoMultiDb with OfferQuestionsPatternDtoMultiDb
    with QuestionsPatternDtoMultiDb with PatternDescriptionsDtoMultiDb with QuestionContentsDtoMultiDb with OffersAppendicesPatternDtoMultiDb with AppendixContentDtoMultiDb
    with OfferSectionsDtoMultiDb with SectionContentsDtoMultiDb with UserDatasourcesDtoMultiDb {

  import driver.api._

  def schemas: driver.DDL =
    DatasourceDao.elements.schema ++
      ProjectDao.projects.schema ++
      ResourceDao.resources.schema ++
      ProjectDatasourceDao.projectDatasources.schema ++
      ProjectResourceDao.projectResources.schema ++
      UserDao.users.schema ++
      ProjectUserDao.projectUsers.schema ++
      ServiceOfferDao.serviceOffers.schema ++
      OfferQuestionPatternDao.offerQuestions.schema ++
      QuestionPatternDao.questionsPattern.schema ++
      ServiceOfferDao.serviceOffers.schema ++
      PatternDescriptionDao.patternDescriptions.schema ++
      QuestionContentDao.questionContents.schema ++
      OfferAppendixPatternDao.offersAppendices.schema ++
      AppendixContentDao.appendixContents.schema ++
      OfferSectionPatternDao.offerSections.schema ++
      SectionContentDao.sectionContents.schema ++
      UserDatasourceDao.userDatasources.schema ++
      FeedbackDao.feedbacks.schema


}
