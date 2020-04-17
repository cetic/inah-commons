package be.cetic.inah.commons.database.sql.population.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import be.cetic.inah.commons.database.sql.population.PopulationResource
import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, DtoCompositeId, SchemaNames}
import slick.dbio.Effect.{Read, Write}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}
import spray.json.{DefaultJsonProtocol, JsValue, RootJsonFormat}
import spray.json._

import scala.concurrent.ExecutionContextExecutor

case class TargetId(populationId: String, order: Int)

case class TargetDto(id: TargetId, delay: String, description: String) extends DtoCompositeId with PopulationResource


trait TargetsDtoMultiDb extends DriverComponent {

  import driver.api._

  class TargetsDto(tag: Tag) extends Table[TargetDto](tag, SchemaNames.populationSchemaName, "target") {
    def populationId = column[String]("population_id")//, O.PrimaryKey)

    def order: Rep[Int] = column[Int]("order")//, O.PrimaryKey)

    def pk = primaryKey("id", (populationId, order))
    def delay = column[String]("delay")

    def description = column[String]("description")

    def targetTupled = (x: (String, Int, String, String)) => TargetDto(TargetId(x._1, x._2), x._3, x._4)

    def targetUnapply = (targetDto: TargetDto) => Some((targetDto.id.populationId, targetDto.id.order, targetDto.delay, targetDto.description))

    def * = (populationId, order, delay, description) <> (targetTupled, targetUnapply)
  }


  implicit val dispatcher: ExecutionContextExecutor

  implicit object TargetDao extends Dao[TargetDto, TargetId] {

    override val thisDriver = driver
    val targets = TableQuery[TargetsDto]

    def create(element: TargetDto) = {
      (targets += element).map(_ => element)
    }

    def update(element: TargetDto): DBIOAction[TargetDto, NoStream, Write] = {
      targets.insertOrUpdate(element).map { _ => element }
    }

    private def queryId(id: TargetId) = targets.filter(p => p.populationId === id.populationId && p.order === id.order)

    def read(id: TargetId): FixedSqlStreamingAction[Seq[TargetDto], TargetDto, Read] = queryId(id).result

    def readAll: FixedSqlStreamingAction[Seq[TargetDto], TargetDto, Read] = targets.result

    def delete(id: TargetId): FixedSqlAction[Int, NoStream, Write] = queryId(id).delete
  }

}

