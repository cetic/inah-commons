package be.cetic.inah.commons.database.sql.management.model

import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import be.cetic.inah.commons.database.sql.management.ManagementResource
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class SubsectionContentDto(projectId : String, sectionPatternId : Int, descriptionId : Int, content : Option[String]) extends ManagementResource

trait SubsectionContentsDtoMultiDb extends DriverComponent with ProjectsDtoMultiDb with PatternDescriptionsDtoMultiDb with OfferSectionPatternsDtoMultiDb {

  import driver.api._

  class SubsectionContentsDto(tag: Tag) extends Table[SubsectionContentDto] (tag, SchemaNames.managementSchemaName, "subsection_content") {
    def projectId = column[String]("project_id")
    def sectionPatternId = column[Int]("section_pattern_id")
    def descriptionId = column[Int]("description_id")
    def content = column[Option[String]]("content")

    def pk = primaryKey("section_content_pk" , (projectId, sectionPatternId, descriptionId))
    def project = foreignKey("section_content_project_fk", projectId, ProjectDao.projects)(_.id)
    def sectionPattern = foreignKey("section_content_section_pattern_fk", sectionPatternId, SectionPatternDao.sectionPatterns)(_.id)
    def description = foreignKey("section_content_description_fk", descriptionId, PatternDescriptionDao.patternDescriptions)(_.id)
    def * = (projectId, sectionPatternId, descriptionId, content) <> (SubsectionContentDto.tupled, SubsectionContentDto.unapply)
  }

  implicit val dispatcher: ExecutionContextExecutor
  implicit object SubsectionContentDao extends Dao[SubsectionContentDto, (String, Int, Int)] {

    val thisDriver = driver
    val subsectionContents = TableQuery[SubsectionContentsDto]

    private def queryById (id : (String, Int, Int)) = subsectionContents.filter(x => x.projectId===id._1 && x.sectionPatternId === id._2 && x.descriptionId === id._3)
    def create(element: SubsectionContentDto): DBIOAction[SubsectionContentDto, NoStream, Effect.Write] = {

      (subsectionContents+=element).map(_=>element)
    }

    def update(element: SubsectionContentDto): DBIOAction[SubsectionContentDto, NoStream, Effect.Write] = {
      (subsectionContents.insertOrUpdate(element)).map(_=>element)
    }

    def read(id: (String, Int, Int)): FixedSqlStreamingAction[Seq[SubsectionContentDto], SubsectionContentDto, Effect.Read] = queryById(id).result

    def readAll = subsectionContents.result

    def delete(id: (String, Int, Int))= queryById(id).delete
  }

}
