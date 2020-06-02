package be.cetic.inah.commons.database.sql.management.model

import be.cetic.inah.commons.database.sql.management.ManagementResource
import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, DtoWithId, SchemaNames}
import slick.dbio.Effect.{Read, Write}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor


case class StatusDto(id: Option[Int], sourceId: Option[Int], status: String, createdAt: Long) extends DtoWithId[Int] with ManagementResource


trait StatusDtoMultiDb extends DriverComponent with DatasourcesDtoMultiDb {

  import driver.api._

  class StatusesDto(tag: Tag) extends Table[StatusDto](tag, SchemaNames.managementSchemaName, "status") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def sourceId = column[Option[Int]]("datasource_id")

    def status = column[String]("content")

    def createdAt = column[Long]("created_at")

    def datasource = foreignKey("datasource_id_fk", sourceId, DatasourceDao.elements)(p => p.id)

    def * = (id.?, sourceId, status,  createdAt) <> (StatusDto.tupled, StatusDto.unapply)
  }


  implicit val dispatcher: ExecutionContextExecutor

  implicit object StatusDao extends Dao[StatusDto, Int] {
    val thisDriver = driver
    val statuses = TableQuery[StatusesDto]
    val elementsAutoInc = statuses returning statuses.map(_.id)

    def create(element: StatusDto): DBIOAction[StatusDto, NoStream, Write] = {
      (elementsAutoInc += element).map(id => element.copy(id = Some(id)))
    }

    def update(element: StatusDto): DBIOAction[StatusDto, NoStream, Write] = {
      elementsAutoInc.insertOrUpdate(element).map { _ => element }
    }

    private def queryId(id: Int): Query[StatusesDto, StatusDto, Seq] = statuses.filter(_.id === id)

    def read(id: Int) = queryId(id).result

    def readAll = statuses.result

    def delete(id: Int): FixedSqlAction[Int, NoStream, Write] = queryId(id).delete
  }

}

