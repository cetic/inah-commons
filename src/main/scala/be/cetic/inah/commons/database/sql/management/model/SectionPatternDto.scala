package be.cetic.inah.commons.database.sql.management.model

import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import be.cetic.inah.commons.database.sql.management.ManagementResource
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class SectionPatternDto (id: Option[Int], descriptionId : Int ) extends ManagementResource

trait SectionPatternsDtoMultiDb  extends DriverComponent with ServiceOffersDtoMultiDb with PatternDescriptionsDtoMultiDb  {
  import driver.api._

  class SectionPatternsDto (tag: Tag) extends Table[SectionPatternDto] (tag, SchemaNames.managementSchemaName, "section_pattern") {

    def id=  column[Int]("id", O.PrimaryKey, O.AutoInc)
    def descriptionId = column[Int]("description_id")
    def description = foreignKey("offer_section_pattern_section",descriptionId,PatternDescriptionDao.patternDescriptions)(_.id)
    def * = (id.?, descriptionId) <> (SectionPatternDto.tupled, SectionPatternDto.unapply)
  }

  implicit val dispatcher: ExecutionContextExecutor

  implicit object SectionPatternDao extends Dao[SectionPatternDto, Int] {
    val thisDriver = driver
    val sectionPatterns = TableQuery[SectionPatternsDto]
    val autoInc = sectionPatterns returning sectionPatterns.map(_.id)

    private def queryById (id: Int)= sectionPatterns.filter(o => o.id === id)
    def create(element: SectionPatternDto): DBIOAction[SectionPatternDto, NoStream, Effect.Write] = {
      (autoInc+=element).map(id=>element.copy(id = Some(id)))
    }

    def update(element: SectionPatternDto): DBIOAction[SectionPatternDto, NoStream, Effect.Write] = {
      (sectionPatterns.insertOrUpdate(element)).map(_=>element)
    }

    def read(id: Int): FixedSqlStreamingAction[Seq[SectionPatternDto], SectionPatternDto, Effect.Read] = queryById(id).result

    def readAll = sectionPatterns.result

    def delete(id: Int)= queryById(id).delete
  }


}
