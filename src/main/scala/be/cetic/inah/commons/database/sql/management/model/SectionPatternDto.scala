package be.cetic.inah.commons.database.sql.management.model

import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import be.cetic.inah.commons.database.sql.management.ManagementResource
import slick.dbio.Effect.{Read, Write}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class SectionPatternDto (id: Option[Int] ,descriptionId : Option[Int] ) extends ManagementResource

trait SectionsPatternDtoMultiDb extends DriverComponent with PatternDescriptionsDtoMultiDb {

  import driver.api._

  class SectionsPatternDto (tag: Tag) extends Table[SectionPatternDto] (tag, SchemaNames.managementSchemaName, "section_pattern") {

    def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
    def descriptionId = column[Option[Int]] ("description_id")
    def description = foreignKey("section_pattern_description_fk", descriptionId, PatternDescriptionDao.patternDescriptions)(_.id)
    def * = (id, descriptionId) <> (SectionPatternDto.tupled, SectionPatternDto.unapply)
  }

  implicit val dispatcher: ExecutionContextExecutor

  implicit object SectionPatternDao extends Dao[SectionPatternDto, Int]{

    val thisDriver = driver

    val sections = TableQuery[SectionsPatternDto]


    private def queryById (id: Int) = sections.filter(s => s.id===id)
    def create(element: SectionPatternDto): DBIOAction[SectionPatternDto, NoStream, Effect.Write] = {

      (sections+=element).map(_=>element)
    }

   def update(element: SectionPatternDto): DBIOAction[SectionPatternDto, NoStream, Effect.Write] = {

     sections.insertOrUpdate(element).map(_=>element)
   }
    def read(id: Int): FixedSqlStreamingAction[Seq[SectionPatternDto], SectionPatternDto, Effect.Read] = queryById(id).result

    def readAll : FixedSqlStreamingAction[Seq[SectionPatternDto], SectionPatternDto, Read] = sections.result

    def delete(id: Int) = queryById(id).delete
  }


}
