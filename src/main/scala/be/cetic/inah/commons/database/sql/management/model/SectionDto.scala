package be.cetic.inah.commons.database.sql.management.model

import java.sql.Blob

import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import be.cetic.inah.commons.database.sql.management.ManagementResource
import slick.dbio.Effect.{Read, Write}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class SectionDto (id: Int , sectionType : String , uploadFile : Blob) extends ManagementResource

trait SectionsDtoMultiDb extends DriverComponent {

  import driver.api._

  class SectionsDto (tag: Tag) extends Table[SectionDto] (tag, SchemaNames.managementSchemaName, "section") {

    def id = column[Int]("id", O.PrimaryKey)
    def sectionType = column[String] ("section_type")
    def uploadFile = column[Blob] ("upload_file")
    def * = (id, sectionType, uploadFile) <> (SectionDto.tupled, SectionDto.unapply)
  }

  implicit val dispatcher: ExecutionContextExecutor

  implicit object SectionDao extends Dao[SectionDto, Int]{

    val thisDriver = driver

    val sections = TableQuery[SectionsDto]


    private def queryById (id: Int) = sections.filter(s => s.id===id)
    def create(element: SectionDto): DBIOAction[SectionDto, NoStream, Effect.Write] = {

      (sections+=element).map(_=>element)
    }

   def update(element: SectionDto): DBIOAction[SectionDto, NoStream, Effect.Write] = {

     sections.insertOrUpdate(element).map(_=>element)
   }
    def read(id: Int): FixedSqlStreamingAction[Seq[SectionDto], SectionDto, Effect.Read] = queryById(id).result

    def readAll : FixedSqlStreamingAction[Seq[SectionDto], SectionDto, Read] = sections.result

    def delete(id: Int) = queryById(id).delete
  }


}
