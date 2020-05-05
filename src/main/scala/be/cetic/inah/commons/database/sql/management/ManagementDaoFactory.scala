package be.cetic.inah.commons.database.sql.management

import be.cetic.inah.commons.database.sql.DaoFactory
import be.cetic.inah.commons.database.sql.access_control.model.TokensDtoMultiDb
import be.cetic.inah.commons.database.sql.management.model._
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContextExecutor

class ManagementDaoFactory(val driver: JdbcProfile, val dbProfile: Option[String] = None)(implicit val dispatcher: ExecutionContextExecutor)
  extends DaoFactory with AppendixContentDtoMultiDb with DatasourcesDtoMultiDb with OffersAppendicesPatternDtoMultiDb with OfferQuestionsPatternDtoMultiDb
    with OfferSectionPatternsDtoMultiDb with PatternDescriptionsDtoMultiDb with ProjectDatasourcesDtoMultiDb with ProjectsDtoMultiDb
    with ProjectOffersDtoMultiDb with ProjectResourcesDtoMultiDb with ProjectUserDetailsDtoMultiDb with QuestionContentsDtoMultiDb with QuestionsPatternDtoMultiDb
    with ResourcesDtoMultiDb with SectionPatternsDtoMultiDb with ServiceOffersDtoMultiDb with SubsectionContentsDtoMultiDb with SubsectionsPatternDtoMultiDb
    with UsersDetailsMultiDb with UsersDtoMultiDb
    with TokensDtoMultiDb {

  import driver.api._

  def schemas: driver.DDL = AppendixContentDao.appendixContents.schema ++
    DatasourceDao.elements.schema ++
    OfferAppendixPatternDao.offersAppendices.schema ++
    OfferQuestionPatternDao.offerQuestions.schema ++
    OfferSectionPatternDao.offerSections.schema ++
    PatternDescriptionDao.patternDescriptions.schema ++
    ProjectDao.projects.schema ++
    ProjectDatasourceDao.projectDatasources.schema ++
    ProjectOfferDao.projectOffers.schema ++
    ProjectResourceDao.projectResources.schema ++
    ProjectUserDetailsDao.projectUsers.schema ++
    QuestionContentDao.questionContents.schema ++
    QuestionPatternDao.questionsPattern.schema ++
    ResourceDao.resources.schema ++
    SectionPatternDao.sectionPatterns.schema ++
    ServiceOfferDao.serviceOffers.schema ++
    SubsectionContentDao.subsectionContents.schema ++
    SubsectionPatternDao.subsectionPatterns.schema ++
    UsersDetailsDao.userDetails.schema ++
    UserDao.users.schema

}
