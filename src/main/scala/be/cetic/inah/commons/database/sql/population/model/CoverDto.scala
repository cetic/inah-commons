package be.cetic.inah.commons.database.sql.population.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import be.cetic.inah.commons.database.sql.population.PopulationResource
import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, DtoWithId, SchemaNames}
import slick.dbio.Effect.{Read, Write}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.ExecutionContextExecutor

case class CoverDto(id: Option[Int], coverOrder: Int, populationId: String, targetOrder: Int, description: String) extends DtoWithId[Int] with PopulationResource


trait CoversDtoMultiDb extends DriverComponent with TargetsDtoMultiDb {

  import driver.api._

  class CoversDto(tag: Tag) extends Table[CoverDto](tag, SchemaNames.populationSchemaName, "cover") {
    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)

    def coverOrder = column[Int]("cover_order")

    def populationId = column[String]("population_id")

    def targetOrder = column[Int]("target_order")

    def targetFk =
      foreignKey("target_order_fk", (populationId, targetOrder), TargetDao.targets)(obj=> (obj.populationId, obj.order))

    def description = column[String]("description")

    def * = (id.?, coverOrder, populationId, targetOrder, description) <> (CoverDto.tupled, CoverDto.unapply)
  }


  implicit val dispatcher: ExecutionContextExecutor

  implicit object CoverDao extends Dao[CoverDto , Int]{
    override val thisDriver = driver
    val covers = TableQuery[CoversDto]
    val coversAutoInc = covers returning covers.map(_.id)

    def create(element: CoverDto) = {
      (coversAutoInc += element).map(id => element.copy(id = Some(id)))
    }

    def update(element: CoverDto): DBIOAction[CoverDto, NoStream, Write] = {
      coversAutoInc.insertOrUpdate(element).map { _ => element }
    }

    private def queryId(id: Int) = covers.filter(_.id === id)

    def read(id: Int): FixedSqlStreamingAction[Seq[CoverDto], CoverDto, Read] = queryId(id).result

    def readAll: FixedSqlStreamingAction[Seq[CoverDto], CoverDto, Read] = covers.result

    def delete(id: Int): FixedSqlAction[Int, NoStream, Write] = queryId(id).delete
  }

}

