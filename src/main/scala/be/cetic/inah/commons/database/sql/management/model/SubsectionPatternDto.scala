package be.cetic.inah.commons.database.sql.management.model

import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import be.cetic.inah.commons.database.sql.management.ManagementResource
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class SubsectionPatternDto(sectionPatternId: Int, descriptionId: Int) extends ManagementResource

trait SubsectionsPatternDtoMultiDb extends DriverComponent with PatternDescriptionsDtoMultiDb with OfferSectionPatternsDtoMultiDb {


  import driver.api._

  class SubsectionPatternsDto(tag: Tag) extends Table[SubsectionPatternDto](tag, SchemaNames.managementSchemaName, "subsection_pattern") {

    def sectionPatternId = column[Int]("section_pattern_id")

    def descriptionId = column[Int]("description_id")

    def pk = primaryKey("section_content_pattern_pk", (sectionPatternId, descriptionId))

    def sectionPattern = foreignKey("section_content_section_fk", sectionPatternId, SectionPatternDao.sectionPatterns)(_.id)

    def description = foreignKey("section_content_pattern_description_fk", descriptionId, PatternDescriptionDao.patternDescriptions)(_.id)

    def * = (sectionPatternId, descriptionId) <> (SubsectionPatternDto.tupled, SubsectionPatternDto.unapply)
  }

  implicit val dispatcher: ExecutionContextExecutor

  implicit object SubsectionPatternDao extends Dao[SubsectionPatternDto, (Int, Int)] {
    val thisDriver = driver
    val subsectionPatterns = TableQuery[SubsectionPatternsDto]

    private def queryById(id: (Int, Int)) = subsectionPatterns.filter(s => s.sectionPatternId === id._1 && s.descriptionId === id._2)

    def create(element: SubsectionPatternDto): DBIOAction[SubsectionPatternDto, NoStream, Effect.Write] = {
      (subsectionPatterns += element).map(_ => element)
    }

    def update(element: SubsectionPatternDto): DBIOAction[SubsectionPatternDto, NoStream, Effect.Write] = {
      (subsectionPatterns.insertOrUpdate(element)).map(_ => element)
    }

    def read(id: (Int, Int)): FixedSqlStreamingAction[Seq[SubsectionPatternDto], SubsectionPatternDto, Effect.Read] = queryById(id).result

    def readAll = subsectionPatterns.result

    def delete(id: (Int, Int)) = queryById(id).delete
  }

}
