package be.cetic.inah.commons.database.sql.management.model

import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import be.cetic.inah.commons.database.sql.management.ManagementResource
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class QuestionContentDto (projectId : String, questionPatternId : Option[Int] , value : String) extends ManagementResource

trait QuestionContentsDtoMultiDb extends ProjectsDtoMultiDb with QuestionsPatternDtoMultiDb with DriverComponent {

  import driver.api._
  class QuestionContentsDto (tag : Tag ) extends Table[QuestionContentDto] (tag , SchemaNames.managementSchemaName, "question_content") {

    def projectId  = column[String]("project_id")
    def questionPatternId = column[Option[Int]]("question_pattern_id")
    def value = column[String]("value")
    def pk = primaryKey("question_content_pk", (projectId, questionPatternId))
    def project = foreignKey("project_project_fk", projectId, ProjectDao.projects)(_.id)
    def questionPattern = foreignKey("project_section_fk",questionPatternId, QuestionPatternDao.questionsPattern)(_.id)
    def * = (projectId,questionPatternId, value) <> (QuestionContentDto.tupled, QuestionContentDto.unapply)

  }

  implicit val dispatcher: ExecutionContextExecutor
  implicit object QuestionContentDao extends Dao[QuestionContentDto, (String, Int)]{
    val thisDriver = driver
    val questionContents = TableQuery[QuestionContentsDto]

    private def queryById (id : (String,Int)) = questionContents.filter (p => p.projectId ===id._1 && p.questionPatternId=== id._2)

    def create(element: QuestionContentDto):DBIOAction[QuestionContentDto, NoStream, Effect.Write] = {

      (questionContents+=element).map(_=>element)
    }

    def update(element: QuestionContentDto): DBIOAction[QuestionContentDto, NoStream, Effect.Write] = {

      (questionContents.insertOrUpdate(element)).map(_=>element)
    }

    def read(id: (String, Int)): FixedSqlStreamingAction[Seq[QuestionContentDto], QuestionContentDto, Effect.Read] = queryById(id).result

    def readAll = questionContents.result

    def delete(id: (String, Int)) = queryById(id).delete
  }


}
