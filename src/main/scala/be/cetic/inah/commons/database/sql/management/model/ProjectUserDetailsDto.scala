package be.cetic.inah.commons.database.sql.management.model

import be.cetic.inah.commons.database.sql.management.ManagementResource
import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import slick.dbio.Effect.Write
import slick.sql.FixedSqlStreamingAction

import scala.concurrent.ExecutionContextExecutor

case class ProjectUserDetailsDto(projectId : String, responsibility: String, userDetailsId : String) extends ManagementResource

trait ProjectUsersDtoMultiDb extends ProjectsDtoMultiDb with UsersDetailsMultiDb with DriverComponent{
import driver.api._

  class ProjectUsersDetailsDto (tag: Tag) extends Table[ProjectUserDetailsDto] (tag, SchemaNames.managementSchemaName, "project_user_detail") {

    def projectId  = column[String]("project_id")
    def email = column[String]("email")
    def pk = primaryKey("project_user_pk", (projectId, email))
    def project = foreignKey("project_project_fk", projectId, ProjectDao.projects)(_.id)
    def user = foreignKey("project_user_fk", email, UsersDetailsDao.userDetails)(_.email)
    def responsibility = column[String]("responsibility")
    def * = (projectId,responsibility, email) <> (ProjectUserDetailsDto.tupled, ProjectUserDetailsDto.unapply)

  }

  implicit val dispatcher: ExecutionContextExecutor

  implicit object ProjectUserDao extends Dao[ProjectUserDetailsDto, (String, String)] {


    val thisDriver = driver

    val projectUsers = TableQuery[ProjectUsersDetailsDto]

    override def create(element: ProjectUserDetailsDto): DBIOAction[ProjectUserDetailsDto, NoStream, Write] = {

      (projectUsers+=element).map(_=>element)
    }

    override def update(element: ProjectUserDetailsDto): DBIOAction[ProjectUserDetailsDto, NoStream, Write] = {

      projectUsers.insertOrUpdate(element).map (_ => element)
    }

    private def readByProjectIdUserId (projectId: String, userDetailsId: String ) = projectUsers.filter(p => (p.projectId === projectId && p.email === userDetailsId)).result

    private def queryId(id: (String, String)) = projectUsers.filter(p => p.projectId === id._1 && p.email === id._2)

    private def readByProjectId (projectId : String ) = {
      projectUsers.filter {
        p => p.projectId === projectId
      }.result
    }

    private def readByUserId (userId : String ) = {

      projectUsers.filter {
        p => p.email === userId
      }
    }

    def delete(id: (String, String)) = queryId(id).delete
    def readAll = projectUsers.result
    def read(id: (String, String)): FixedSqlStreamingAction[Seq[ProjectUserDetailsDto], ProjectUserDetailsDto, Effect.Read] = queryId(id).result


  }




}
