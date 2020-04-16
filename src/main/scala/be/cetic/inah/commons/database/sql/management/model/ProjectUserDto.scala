package be.cetic.inah.commons.database.sql.management.model

import be.cetic.inah.commons.database.sql.management.Management
import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, ManagementResource, SchemaName}
import slick.dbio.Effect.Write
import slick.sql.FixedSqlStreamingAction

import scala.concurrent.ExecutionContextExecutor

case class ProjectUserDto (projectId : String, userId : String) extends ManagementResource

trait ProjectUsersDtoMultiDb extends ProjectsDtoMultiDb with UsersDtoMultiDb with DriverComponent with Management with SchemaName{
import driver.api._

  class ProjectUsersDto (tag: Tag) extends Table[ProjectUserDto] (tag, schemaName.orElse(managementSchemaName), "project_user") {

    def projectId  = column[String]("project_id")
    def userId = column[String]("user_id")
    def pk = primaryKey("project_user_pk", (projectId, userId))
    def project = foreignKey("project_project_fk", projectId, ProjectDao.projects)(_.id)
    def user = foreignKey("project_user_fk", userId, UserDao.users)(_.email)
    def * = (projectId, userId) <> (ProjectUserDto.tupled, ProjectUserDto.unapply)

  }

  implicit val dispatcher: ExecutionContextExecutor

  implicit object ProjectUserDao extends Dao[ProjectUserDto, (String, String)] {


    val thisDriver = driver

    val projectUsers = TableQuery[ProjectUsersDto]

    override def create(element: ProjectUserDto): DBIOAction[ProjectUserDto, NoStream, Write] = {

      (projectUsers+=element).map(_=>element)
    }

    override def update(element: ProjectUserDto): DBIOAction[ProjectUserDto, NoStream, Write] = {

      projectUsers.insertOrUpdate(element).map (_ => element)
    }

    private def readByProjectIdUserId (projectId: String, userId: String ) = projectUsers.filter(p => (p.projectId === projectId && p.userId === userId)).result

    private def queryId(id: (String, String)) = projectUsers.filter(p => p.projectId === id._1 && p.userId === id._2)

    private def readByProjectId (projectId : String ) = {
      projectUsers.filter {
        p => p.projectId === projectId
      }.result
    }

    private def readByUserId (userId : String ) = {

      projectUsers.filter {
        p => p.userId === userId
      }
    }

    override def delete(id: (String, String)) = queryId(id).delete
    override def readAll = projectUsers.result
    override def read(id: (String, String)): FixedSqlStreamingAction[Seq[ProjectUserDto], ProjectUserDto, Effect.Read] = queryId(id).result


  }




}
