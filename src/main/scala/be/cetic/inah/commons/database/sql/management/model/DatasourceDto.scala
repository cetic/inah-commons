package be.cetic.inah.commons.database.sql.management.model

import be.cetic.inah.commons.database.sql.enumeration.DatasourceStatus
import be.cetic.inah.commons.database.sql.management.ManagementResource
import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, DtoWithId, SchemaNames}
import slick.dbio.Effect.{Read, Write}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor


case class DatasourceDto(id: Option[Int], name: String, serviceUrl: String, satus: String = DatasourceStatus.UP, createdAt: Long) extends DtoWithId[Int] with ManagementResource

trait DatasourcesDtoMultiDb extends DriverComponent {

  import driver.api._

  class DatasourcesDto(tag: Tag) extends Table[DatasourceDto](tag, SchemaNames.managementSchemaName, "datasource") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def serviceUrl = column[String]("service_url")

    def status = column[String]("status")

    def createdAt = column[Long]("created_at")

    def * = (id.?, name, serviceUrl, status, createdAt) <> (DatasourceDto.tupled, DatasourceDto.unapply)
  }


  implicit val dispatcher: ExecutionContextExecutor

  implicit object DatasourceDao extends Dao[DatasourceDto, Int] {
    val thisDriver = driver
    val elements = TableQuery[DatasourcesDto]
    val elementsAutoInc = elements returning elements.map(_.id)

    def create(element: DatasourceDto): DBIOAction[DatasourceDto, NoStream, Write] = {
      (elementsAutoInc += element).map(id => element.copy(id = Some(id)))
    }

    def update(element: DatasourceDto): DBIOAction[DatasourceDto, NoStream, Write] = {
      elementsAutoInc.insertOrUpdate(element).map { _ => element }
    }

    private def queryId(id: Int) = elements.filter(_.id === id)

    def read(id: Int): FixedSqlStreamingAction[Seq[DatasourceDto], DatasourceDto, Read] = queryId(id).result

    def readAll: FixedSqlStreamingAction[Seq[DatasourceDto], DatasourceDto, Read] = elements.result

    def delete(id: Int): FixedSqlAction[Int, NoStream, Write] = queryId(id).delete
  }

}

