package be.cetic.inah.commons.database.sql.management.model

import java.util.UUID

import be.cetic.inah.commons.database.sql.access_control.model.TokensDtoMultiDb
import be.cetic.inah.commons.database.sql.enumeration.ProjectStatus
import be.cetic.inah.commons.database.sql.management.Management
import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, Dto, ManagementResource, SchemaName}
import slick.dbio.Effect.{Read, Write}
import slick.sql.FixedSqlStreamingAction

import scala.concurrent.ExecutionContextExecutor


case class ProjectDto(id: Option[String], name: String, status: String = ProjectStatus.PENDING, createdAt: Long = System.currentTimeMillis() / 1000, tokenId: Option[Int]) extends ManagementResource {
  def toDb = {
    val idDb = id.getOrElse(UUID.randomUUID().toString)
    ProjectDbDto(idDb, name, status, createdAt, tokenId)
  }

  def toView = this.copy(tokenId = None)
}

case class ProjectDbDto(id: String, name: String, status: String = ProjectStatus.PENDING, createdAt: Long = System.currentTimeMillis() / 1000, tokenId: Option[Int]) extends Dto


trait ProjectsDtoMultiDb extends TokensDtoMultiDb with DriverComponent with Management with SchemaName {
  import driver.api._

  class ProjectsDto(tag: Tag) extends Table[ProjectDto](tag, schemaName.orElse(managementSchemaName), "project") {
    def id = column[String]("id", O.PrimaryKey)

    def name = column[String]("name")

    def status = column[String]("status")

    def createdAt = column[Long]("createdAt")

    def tokenId = column[Option[Int]]("token_id")

    def token = foreignKey("projects_token_fk", tokenId, TokensDao.tokens)(_.id)

    def projectTupled = (x: (String, String, String, Long, Option[Int])) => {
      ProjectDto.tupled(x.copy(_1 = Some(x._1)))
    }

    def projectUnapply = (p: ProjectDto) => ProjectDbDto.unapply(p.toDb)


    def * = (id, name, status, createdAt, tokenId) <> (projectTupled, projectUnapply)
  }


  implicit val dispatcher: ExecutionContextExecutor

  implicit object ProjectDao extends Dao[ProjectDto, String] {
    val thisDriver = driver

    val projects = TableQuery[ProjectsDto]

    def create(element: ProjectDto): DBIOAction[ProjectDto, NoStream, Write] = {
      (projects += element).map(_ => element)
    }

    def update(element: ProjectDto): DBIOAction[ProjectDto, NoStream, Write] = {
      projects.insertOrUpdate(element).map { _ => element }
    }

    private def queryId(id: String) = projects.filter(pc => pc.id === id)

    def read(id: String): FixedSqlStreamingAction[Seq[ProjectDto], ProjectDto, Read] = queryId(id).result

    def readAll: FixedSqlStreamingAction[Seq[ProjectDto], ProjectDto, Read] = projects.result

    def delete(id: String) = queryId(id).delete
  }

}

