package be.cetic.inah.commons.database.sql.management.model

import be.cetic.inah.commons.database.sql.management.ManagementResource
import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import slick.dbio.Effect.{Read, Write}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class ProjectResourceDto(projectId: String, resourceId: Int) extends ManagementResource



trait ProjectResourcesDtoMultiDb extends ProjectsDtoMultiDb with DriverComponent with ResourcesDtoMultiDb{
  import driver.api._

  class ProjectResourcesDto(tag: Tag) extends Table[ProjectResourceDto](tag, SchemaNames.managementSchemaName, "project_resource") {
    def projectId = column[String]("project_id")

    def resourceId = column[Int]("resource_id")

    def pk = primaryKey("project_resource_pk", (projectId, resourceId))

    def project = foreignKey("project_datasource_project_fk", projectId, ProjectDao.projects)(_.id)

    def resource = foreignKey("project_resource_resource_fk", resourceId, ResourceDao.resources)(_.id)

    def * = (projectId, resourceId) <> (ProjectResourceDto.tupled, ProjectResourceDto.unapply)
  }


  implicit val dispatcher: ExecutionContextExecutor

  implicit object ProjectResourceDao extends Dao[ProjectResourceDto, (String, Int)] {
    val thisDriver = driver
    val projectResources = TableQuery[ProjectResourcesDto]

    def create(element: ProjectResourceDto): DBIOAction[ProjectResourceDto, NoStream, Write] = {
      (projectResources += element).map(_ => element)
    }

    def update(element: ProjectResourceDto): DBIOAction[ProjectResourceDto, NoStream, Write] = {
      projectResources.insertOrUpdate(element).map { _ => element }
    }

    private def queryId(projectId: String, sourceId: Int) = projectResources.filter(pc => pc.projectId === projectId && pc.resourceId === sourceId)

    private def queryByProjectId(projectId: String): Query[ProjectResourcesDto, ProjectResourceDto, Seq] = projectResources.filter(pc => pc.projectId === projectId)

    private def queryBySourceId(sourceId: Int): Query[ProjectResourcesDto, ProjectResourceDto, Seq] = projectResources.filter(pc => pc.resourceId === sourceId)

    def readBySource(id: Int): FixedSqlStreamingAction[Seq[ProjectResourceDto], ProjectResourceDto, Read] = queryBySourceId(id).result

    def readByProject(id: String): FixedSqlStreamingAction[Seq[ProjectResourceDto], ProjectResourceDto, Read] = queryByProjectId(id).result

    def readAll: FixedSqlStreamingAction[Seq[ProjectResourceDto], ProjectResourceDto, Read] = projectResources.result

    def read(id: (String, Int)) = queryId(id._1, id._2).result

    def delete(id: (String, Int)): FixedSqlAction[Int, NoStream, Write] = queryId(id._1, id._2).delete
  }

}

