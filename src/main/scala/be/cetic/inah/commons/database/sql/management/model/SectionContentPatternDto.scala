package be.cetic.inah.commons.database.sql.management.model

import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import be.cetic.inah.commons.database.sql.management.ManagementResource
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class SectionContentPatternDto (sectionPatternId : Option[Int], descriptionId : Option[Int]) extends ManagementResource

trait SectionsContentPatternDtoMultiDb extends DriverComponent with PatternDescriptionsDtoMultiDb with SectionsPatternDtoMultiDb {


  import driver.api._
  class SectionsContentPatternDto (tag: Tag) extends Table[SectionContentPatternDto] (tag, SchemaNames.managementSchemaName, "section_content_pattern") {

    def sectionPatternId = column[Option[Int]]("section_pattern_id")
    def descriptionId = column[Option[Int]]("description_id")
    def pk = primaryKey("section_content_pattern_pk", (sectionPatternId, descriptionId))
    def sectionPattern = foreignKey("section_content_section_fk", sectionPatternId, SectionPatternDao.sections)(_.id)
    def description = foreignKey("section_content_pattern_description_fk", descriptionId, PatternDescriptionDao.patternDescriptions)(_.id)
    def * = (sectionPatternId, descriptionId) <> (SectionContentPatternDto.tupled, SectionContentPatternDto.unapply)
  }

  implicit val dispatcher: ExecutionContextExecutor
  implicit object SectionContentPatternDao extends Dao[SectionContentPatternDto, (Int, Int)] {
    val thisDriver = driver
    val sectionContents = TableQuery[SectionsContentPatternDto]

    private def queryById (id : (Int, Int)) = sectionContents.filter(s => s.sectionPatternId === id._1 && s.descriptionId === id._2)
    def create(element: SectionContentPatternDto): DBIOAction[SectionContentPatternDto, NoStream, Effect.Write] = {

      (sectionContents+=element).map(_=>element)
    }

    def update(element: SectionContentPatternDto): DBIOAction[SectionContentPatternDto, NoStream, Effect.Write] = {


      (sectionContents.insertOrUpdate(element)).map(_=>element)
    }

    def read(id: (Int, Int)): FixedSqlStreamingAction[Seq[SectionContentPatternDto], SectionContentPatternDto, Effect.Read] = queryById(id).result

    def readAll = sectionContents.result

    def delete(id: (Int, Int))= queryById(id).delete
  }
}
