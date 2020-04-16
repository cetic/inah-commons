package be.cetic.inah.commons.database.sql.management.model

import be.cetic.inah.commons.database.sql.management.Management
import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, DtoWithId, ManagementResource, SchemaName}
import slick.dbio.Effect.{Read, Schema, Write}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class ResourceDto(id: Option[Int], name: String, `type`: String, value: String,
                       status: Option[String], createdAt: Option[Long]) extends DtoWithId[Int] with ManagementResource{
  def withCreated(): ResourceDto= {
    val finalCreatedAt = createdAt.getOrElse( System.currentTimeMillis() / 1000)
    this.copy(createdAt=Some(finalCreatedAt))
  }
}


trait ResourcesDtoMultiDb extends DriverComponent with Management with SchemaName{

  import driver.api._

  class ResourcesDto(tag: Tag) extends Table[ResourceDto](tag, schemaName.orElse(managementSchemaName), "resource") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def value = column[String]("value")

    def `type` = column[String]("type")

    def status = column[Option[String]]("status")

    def createdAt = column[Option[Long]]("createdAt")

    def * = (id.?, name, `type`, value, status, createdAt) <> (ResourceDto.tupled, ResourceDto.unapply)
  }

  implicit val dispatcher: ExecutionContextExecutor

  implicit object ResourceDao extends Dao[ResourceDto, Int]
  {
    val thisDriver = driver
    val resources = TableQuery[ResourcesDto]
    val resourceAutoInc = resources returning resources.map(_.id)


    def create(element: ResourceDto): DBIOAction[ResourceDto, NoStream, Write] = {
      (resourceAutoInc += element.withCreated()).map(id => element.copy(id = Some(id)))
      (resourceAutoInc += element.withCreated()).map(id => element.copy(id = Some(id)))
    }

    def update(element: ResourceDto): DBIOAction[ResourceDto, NoStream, Write] = {
      resources.insertOrUpdate(element).map { _ => element }
    }

    private def queryId(id: Int)

    = resources.filter(pc => pc.id === id)

    def read(id: Int): FixedSqlStreamingAction[Seq[ResourceDto], ResourceDto, Read] = queryId(id).result

    def readAll: FixedSqlStreamingAction[Seq[ResourceDto], ResourceDto, Read] = resources.result

    def delete(id: Int): FixedSqlAction[Int, NoStream, Write] = queryId(id).delete
  }

}

