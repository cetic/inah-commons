package be.cetic.inah.commons.database.sql.management.model

import java.sql.Blob

import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import be.cetic.inah.commons.database.sql.management.ManagementResource
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class AppendixContentDto (projectId : String, descriptionId : Option[Int], content : Blob) extends ManagementResource

trait AppendixContentDtoMultiDb extends DriverComponent with ProjectsDtoMultiDb with PatternDescriptionsDtoMultiDb {

  import driver.api._

  class AppendicesContentDto (tag: Tag) extends Table[AppendixContentDto] (tag, SchemaNames.managementSchemaName, "appendix_content") {

    def projectId = column[String]("project_id")
    def descriptionId = column[Option[Int]]("description_id")
    def content = column[Blob]("content")
    def pk = primaryKey("appendix_content_pk", (projectId, descriptionId))
    def project = foreignKey("appendix_content_project_fk", projectId, ProjectDao.projects)(_.id)
    def description = foreignKey("appendix_content_description_fk", descriptionId, PatternDescriptionDao.patternDescriptions)(_.id)
    def * = (projectId, descriptionId, content) <> (AppendixContentDto.tupled, AppendixContentDto.unapply)

  }

  implicit val dispatcher: ExecutionContextExecutor
  implicit object AppendixContentDao extends Dao[AppendixContentDto, (String,Int)] {
    val thisDriver = driver

    val appendixContents = TableQuery[AppendicesContentDto]
    private def queryById (id: (String, Int)) = appendixContents.filter(a =>a.projectId===id._1 && a.descriptionId===id._2)
    def create(element: AppendixContentDto): DBIOAction[AppendixContentDto, NoStream, Effect.Write] = {

      (appendixContents+=element).map(_=>element)
    }

    def update(element: AppendixContentDto): DBIOAction[AppendixContentDto, NoStream, Effect.Write] = {

      (appendixContents.insertOrUpdate(element)).map(_=>element)
    }

    def read(id: (String, Int)): FixedSqlStreamingAction[Seq[AppendixContentDto], AppendixContentDto, Effect.Read] = queryById(id).result

    def readAll = appendixContents.result

    def delete(id: (String, Int)) = queryById(id).delete
  }


}
