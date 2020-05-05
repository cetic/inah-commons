package be.cetic.inah.commons.database.sql.management.model

import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import be.cetic.inah.commons.database.sql.management.ManagementResource
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class PatternDescriptionDto (id : Option[Int], name : String, description : String ) extends ManagementResource
trait PatternDescriptionsDtoMultiDb extends DriverComponent {
  import driver.api._

  class PatternDescriptionsDto (tag : Tag ) extends Table[PatternDescriptionDto] (tag, SchemaNames.managementSchemaName, "pattern_description") {


    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def description = column[String]("description")
    def * = (id.?, name, description) <> (PatternDescriptionDto.tupled, PatternDescriptionDto.unapply)
  }

  implicit val dispatcher: ExecutionContextExecutor

  implicit object PatternDescriptionDao extends Dao[PatternDescriptionDto, Int] {

    val thisDriver = driver
    val patternDescriptions = TableQuery[PatternDescriptionsDto]
    val autoInc = patternDescriptions returning patternDescriptions.map(_.id)

    private def queryById (id : Int) = patternDescriptions.filter(p => p.id===id)
    def create(element: PatternDescriptionDto): DBIOAction[PatternDescriptionDto,NoStream, Effect.Write] = {
      (autoInc+=element).map(id=>element.copy(id = Some(id)))
    }

    def update(element: PatternDescriptionDto): DBIOAction[PatternDescriptionDto, NoStream, Effect.Write] = {
      (patternDescriptions.insertOrUpdate(element)).map(_=>element)
    }

    def read(id: Int): FixedSqlStreamingAction[Seq[PatternDescriptionDto], PatternDescriptionDto, Effect.Read] = queryById(id).result

    def readAll = patternDescriptions.result

    def delete(id: Int) = queryById(id).delete
  }





}
