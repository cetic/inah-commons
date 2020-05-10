package be.cetic.inah.commons.database.sql.management.model

import be.cetic.inah.commons.database.sql.management.ManagementResource
import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, DtoWithId, SchemaNames}
import slick.dbio.Effect.{Read, Write}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor


case class FeedbackDto(id: Option[Int], projectId: String, sourceId: Option[Int], content: String, createdAt: Long) extends DtoWithId[Int] with ManagementResource

object Feedback {
  def apply(projectId: String, content: String): FeedbackDto = FeedbackDto(None, projectId, None, content, System.currentTimeMillis())
  def apply(projectId: String, sourceId: Int, content: String): FeedbackDto = FeedbackDto(None, projectId, Some(sourceId), content, System.currentTimeMillis())
}

trait FeedbackDtoMultiDb extends DriverComponent with DatasourcesDtoMultiDb with ProjectsDtoMultiDb {

  import driver.api._

  class FeedbacksDto(tag: Tag) extends Table[FeedbackDto](tag, SchemaNames.managementSchemaName, "feedback") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def projectId = column[String]("project_id")

    def sourceId = column[Option[Int]]("datasource_id")

    def content = column[String]("content")

    def createdAt = column[Long]("created_at")


    def project = foreignKey("feedback_project_fk", projectId, ProjectDao.projects)(p => p.id)

    def source = foreignKey("feedback_source_fk", sourceId, DatasourceDao.elements)(s => s.id)

    def * = (id.?, projectId, sourceId, content, createdAt) <> (FeedbackDto.tupled, FeedbackDto.unapply)
  }


  implicit val dispatcher: ExecutionContextExecutor

  implicit object FeedbackDao extends Dao[FeedbackDto, Int] {
    val thisDriver = driver
    val feedbacks = TableQuery[FeedbacksDto]
    val elementsAutoInc = feedbacks returning feedbacks.map(_.id)

    def create(element: FeedbackDto): DBIOAction[FeedbackDto, NoStream, Write] = {
      (elementsAutoInc += element).map(id => element.copy(id = Some(id)))
    }

    def update(element: FeedbackDto): DBIOAction[FeedbackDto, NoStream, Write] = {
      elementsAutoInc.insertOrUpdate(element).map { _ => element }
    }

    private def queryId(id: Int) = feedbacks.filter(_.id === id)

    def read(id: Int): FixedSqlStreamingAction[Seq[FeedbackDto], FeedbackDto, Read] = queryId(id).result

    def readAll: FixedSqlStreamingAction[Seq[FeedbackDto], FeedbackDto, Read] = feedbacks.result

    def delete(id: Int): FixedSqlAction[Int, NoStream, Write] = queryId(id).delete
  }

}

