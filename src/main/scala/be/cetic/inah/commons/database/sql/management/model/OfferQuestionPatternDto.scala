package be.cetic.inah.commons.database.sql.management.model

import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import be.cetic.inah.commons.database.sql.management.ManagementResource
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class OfferQuestionPatternDto (offerId : Int, questionId : Int) extends ManagementResource
trait OfferQuestionsPatternDtoMultiDb extends DriverComponent with ServiceOffersDtoMultiDb with QuestionsPatternDtoMultiDb {
  import driver.api._
  class OfferQuestionsDto (tag: Tag) extends Table[OfferQuestionPatternDto] (tag, SchemaNames.managementSchemaName, "offer_question_pattern") {
    def offerId = column[Int]("offer_id")
    def questionId = column[Int]("question_id")
    def pk = primaryKey("offer_question_pk", (offerId, questionId))
    def offer = foreignKey("offer_question_pattern_offer_fk", offerId, ServiceOfferDao.serviceOffers)(_.id)
    def question = foreignKey("offer_question_pattern_question_fk", questionId, QuestionPatternDao.questionsPattern)(_.id)
    def * = (offerId, questionId) <> (OfferQuestionPatternDto.tupled, OfferQuestionPatternDto.unapply)
  }

  implicit val dispatcher: ExecutionContextExecutor

  implicit object OfferQuestionPatternDao extends Dao[OfferQuestionPatternDto, (Int, Int)] {
    val thisDriver = driver
    val offerQuestions = TableQuery[OfferQuestionsDto]
    private def queryById (id: (Int, Int)) = offerQuestions.filter(o => o.offerId ===id._1 && o.questionId === id._2)

    def create(element: OfferQuestionPatternDto): DBIOAction[OfferQuestionPatternDto, NoStream, Effect.Write] = {
      (offerQuestions+=element).map(_=>element)
    }

    def update(element: OfferQuestionPatternDto): DBIOAction[OfferQuestionPatternDto, NoStream, Effect.Write] = {

      (offerQuestions.insertOrUpdate(element)).map(_=>element)
    }

    def read(id: (Int, Int)): FixedSqlStreamingAction[Seq[OfferQuestionPatternDto], OfferQuestionPatternDto, Effect.Read] = queryById(id).result

    def readAll = offerQuestions.result

    def delete(id: (Int, Int))= queryById(id).delete
  }
}
