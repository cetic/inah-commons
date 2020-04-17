package be.cetic.inah.commons.database.sql.management.model

import be.cetic.inah.commons.database.sql.management.ManagementResource
import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import slick.dbio.Effect.Write
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class ProjectDatasourceDto(projectId: String, dataSourceId: Int, acceptedAt: Option[Long] = Some(System.currentTimeMillis() / 1000)) extends ManagementResource


trait ProjectDatasourcesDtoMultiDb extends ProjectsDtoMultiDb with DriverComponent with DatasourcesDtoMultiDb {

  import driver.api._

  class ProjectDatasourcesDto(tag: Tag) extends Table[ProjectDatasourceDto](tag, SchemaNames.managementSchemaName, "project_datasource") {
    def projectId: Rep[String] = column[String]("project_id")

    def datasourceId: Rep[Int] = column[Int]("datasource_id")

    def acceptedAt: Rep[Option[Long]] = column[Option[Long]]("accepted_at")

    def pk = primaryKey("project_data_source_pk", (projectId, datasourceId))

    def project = foreignKey("project_datasource_project_fk", projectId, ProjectDao.projects)(_.id)

    def datasource = foreignKey("project_datasource_datasource_fk", datasourceId, DatasourceDao.elements)(_.id)

    def * = (projectId, datasourceId, acceptedAt) <> (ProjectDatasourceDto.tupled, ProjectDatasourceDto.unapply)
  }


  implicit val dispatcher: ExecutionContextExecutor

  implicit object ProjectDatasourceDao extends Dao[ProjectDatasourceDto, (String, Int)] {
    val thisDriver = driver

    val projectDatasources = TableQuery[ProjectDatasourcesDto]

    def create(element: ProjectDatasourceDto): DBIOAction[ProjectDatasourceDto, NoStream, Write] = {
      (projectDatasources += element).map(_ => element)
    }

    def update(element: ProjectDatasourceDto): DBIOAction[ProjectDatasourceDto, NoStream, Write] = {
      projectDatasources.insertOrUpdate(element).map { _ => element }
    }

    private def queryId(projectId: String, sourceId: Int) = projectDatasources.filter(pc => pc.projectId === projectId && pc.datasourceId === sourceId)

    private def queryByProjectId(projectId: String) = projectDatasources.filter(pc => pc.projectId === projectId)

    private def queryBySourceId(sourceId: Int) = projectDatasources.filter(pc => pc.datasourceId === sourceId)

    def readBySource(id: Int): FixedSqlStreamingAction[Seq[ProjectDatasourceDto], ProjectDatasourceDto, Effect.Read] = queryBySourceId(id).result

    def readByProject(id: String): FixedSqlStreamingAction[Seq[ProjectDatasourceDto], ProjectDatasourceDto, Effect.Read] = queryByProjectId(id).result

    def readAll: FixedSqlStreamingAction[Seq[ProjectDatasourceDto], ProjectDatasourceDto, Effect.Read] = projectDatasources.result

    def delete(projectId: String, sourceId: Int): FixedSqlAction[Int, NoStream, Write] = queryId(projectId, sourceId).delete

    def queryId(id: (String, Int)) = projectDatasources.filter(pc => pc.projectId === id._1 && pc.datasourceId === id._2)

    override def read(id: (String, Int)) = queryId(id).result

    override def delete(id: (String, Int)) = queryId(id).delete
  }

}

