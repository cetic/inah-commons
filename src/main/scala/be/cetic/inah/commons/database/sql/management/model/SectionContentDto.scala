package be.cetic.inah.commons.database.sql.management.model

import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import be.cetic.inah.commons.database.sql.management.ManagementResource
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class SectionContentDto (projectId : String, sectionPatternId : Option[Int], descriptionId : Option[Int], content : String) extends ManagementResource

trait SectionContentsDtoMultiDb extends DriverComponent with ProjectsDtoMultiDb with PatternDescriptionsDtoMultiDb with SectionsPatternDtoMultiDb {

  import driver.api._

  class SectionContentsDto (tag: Tag) extends Table[SectionContentDto] (tag, SchemaNames.managementSchemaName, "section_pattern") {

    def projectId = column[String]("project_id")
    def sectionPatternId = column[Option[Int]]("section_pattern_id")
    def descriptionId = column[Option[Int]]("description_id")
    def content = column[String]("content")

    def pk = primaryKey("section_content_pk" , (projectId, sectionPatternId, descriptionId))
    def project = foreignKey("section_content_project_fk", projectId, ProjectDao.projects)(_.id)
    def sectionPattern = foreignKey("section_content_section_pattern_fk", sectionPatternId, SectionPatternDao.sections)(_.id)
    def description = foreignKey("section_content_description_fk", descriptionId, PatternDescriptionDao.patternDescriptions)(_.id)
    def * = (projectId, sectionPatternId, descriptionId, content) <> (SectionContentDto.tupled, SectionContentDto.unapply)
  }

  implicit val dispatcher: ExecutionContextExecutor
  implicit object SectionContentDao extends Dao[SectionContentDto, (String, Int, Int)] {

    val thisDriver = driver
    val sectionContents = TableQuery[SectionContentsDto]

    private def queryById (id : (String, Int, Int)) = sectionContents.filter(x => x.projectId===id._1 && x.sectionPatternId === id._2 && x.descriptionId === id._3)
    def create(element: SectionContentDto): DBIOAction[SectionContentDto, NoStream, Effect.Write] = {

      (sectionContents+=element).map(_=>element)
    }

    def update(element: SectionContentDto): DBIOAction[SectionContentDto, NoStream, Effect.Write] = {
      (sectionContents.insertOrUpdate(element)).map(_=>element)
    }

    def read(id: (String, Int, Int)): FixedSqlStreamingAction[Seq[SectionContentDto], SectionContentDto, Effect.Read] = queryById(id).result

    def readAll = sectionContents.result

    def delete(id: (String, Int, Int))= queryById(id).delete
  }

}
