package be.cetic.inah.commons.database.sql.management.model

import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import be.cetic.inah.commons.database.sql.management.ManagementResource
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor


case class QuestionPatternDto (id : Option[Int], options : Seq[String], descriptionId : Option[Int]) extends ManagementResource

trait QuestionsPatternDtoMultiDb extends DriverComponent with PatternDescriptionsDtoMultiDb {
  import driver.api._

  class QuestionsPatternDto (tag: Tag) extends Table[QuestionPatternDto] (tag, SchemaNames.managementSchemaName, "question_pattern") {

    def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
    def options = column[String]("options")
    def descriptionId = column[Option[Int]]("description_id")
    def description = foreignKey("question_pattern_description_fk", descriptionId, PatternDescriptionDao.patternDescriptions)(_.id)

    def questionPatternTupled = (x : (Option[Int], String, Option[Int])) => QuestionPatternDto(x._1, x._2.split(";"), x._3)
    def questionPatternUnapply : QuestionPatternDto => Option[(Option[Int], String, Option[Int])] = (o: QuestionPatternDto) => Some(o.id, o.options.mkString(";"), o.descriptionId)

    def * =(id, options, descriptionId) <> (questionPatternTupled, questionPatternUnapply)
  }

  implicit val dispatcher: ExecutionContextExecutor
  implicit object QuestionPatternDao extends Dao[QuestionPatternDto, Int] {
    val thisDriver = driver
    val questionsPattern = TableQuery[QuestionsPatternDto]

    private def queryById (id: Int) = questionsPattern.filter(q => q.id===id)
    def create(element: QuestionPatternDto):DBIOAction[QuestionPatternDto,NoStream, Effect.Write] = {

      (questionsPattern+=element).map(_=>element)
    }

    def update(element: QuestionPatternDto): DBIOAction[QuestionPatternDto, NoStream, Effect.Write] = {

      (questionsPattern.insertOrUpdate(element)).map(_=>element)
    }

    def read(id: Int): FixedSqlStreamingAction[Seq[QuestionPatternDto], QuestionPatternDto, Effect.Read] = queryById(id).result

    def readAll = questionsPattern.result

    def delete(id: Int)= queryById(id).delete
  }


}
