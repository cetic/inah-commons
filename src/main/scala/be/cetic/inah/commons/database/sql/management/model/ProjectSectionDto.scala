package be.cetic.inah.commons.database.sql.management.model

import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import be.cetic.inah.commons.database.sql.management.ManagementResource
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class ProjectSectionDto (projectId : String, sectionId : Int ) extends ManagementResource
trait ProjectSectionsDtoMultiDb extends ProjectsDtoMultiDb with SectionsDtoMultiDb with DriverComponent {

  import driver.api._
  class ProjectSectionsDto (tag : Tag ) extends Table[ProjectSectionDto] (tag , SchemaNames.managementSchemaName, "project_section") {

    def projectId  = column[String]("project_id")
    def sectionId = column[Int]("section_id")
    def pk = primaryKey("project_section_pk", (projectId, sectionId))
    def project = foreignKey("project_project_fk", projectId, ProjectDao.projects)(_.id)
    def section = foreignKey("project_section_fk",sectionId, SectionDao.sections)(_.id)
    def * = (projectId,sectionId) <> (ProjectSectionDto.tupled, ProjectSectionDto.unapply)

  }

  implicit val dispatcher: ExecutionContextExecutor
  implicit object ProjectSectionDao extends Dao[ProjectSectionDto, (String, Int)]{
    val thisDriver = driver
    val projectSections = TableQuery[ProjectSectionsDto]

    private def queryById (id : (String,Int )) = projectSections.filter (p => p.projectId ===id._1 && p.sectionId === id._2)
    def create(element: ProjectSectionDto): DBIOAction[ProjectSectionDto, NoStream, Effect.Write] = {
      (projectSections+=element).map(_=>element)
    }

    def update(element: ProjectSectionDto): DBIOAction[ProjectSectionDto,NoStream, Effect.Write] = {

      projectSections.insertOrUpdate(element).map(_=>element)
    }

    def read(id: (String, Int)): FixedSqlStreamingAction[Seq[ProjectSectionDto], ProjectSectionDto, Effect.Read] = queryById(id).result

    def readAll = projectSections.result

    def delete(id: (String, Int)): FixedSqlAction[Int, NoStream, Effect.Write] = queryById(id).delete
  }


}
