package be.cetic.inah.commons.database.sql.population.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import be.cetic.inah.commons.database.sql.population.PopulationResource
import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, DtoWithProvidedId, SchemaNames}
import slick.dbio.Effect.{Read, Write}
import slick.sql.SqlProfile.ColumnOption.Nullable
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.ExecutionContextExecutor



case class PopulationDto(id: String, description: String, status: String, nLocal: Option[Int] = None,
                         nSync: Option[Int] = None) extends DtoWithProvidedId[String] with PopulationResource



trait PopulationsDtoMultiDb extends DriverComponent {

  import driver.api._

  class PopulationsDto(tag: Tag) extends Table[PopulationDto](tag, SchemaNames.populationSchemaName, "population") {
    def id = column[String]("id", O.PrimaryKey)

    def description = column[String]("description")

    def status = column[String]("status")

    def nLocal = column[Int]("n_local", Nullable)

    def nSync = column[Int]("n_sync", Nullable)

    def * = (id, description, status, nLocal.?, nSync.?) <> (PopulationDto.tupled, PopulationDto.unapply)
  }


  implicit val dispatcher: ExecutionContextExecutor

  implicit object PopulationDao extends Dao[PopulationDto, String]{
    override val thisDriver = driver
    val populations = TableQuery[PopulationsDto]

    def create(element: PopulationDto) = {
      (populations += element).map(_ => element)
    }

    def update(element: PopulationDto): DBIOAction[PopulationDto, NoStream, Write] = {
      populations.insertOrUpdate(element).map { _ => element }
    }

    private def queryId(id: String) = populations.filter(_.id === id)

    def read(id: String): FixedSqlStreamingAction[Seq[PopulationDto], PopulationDto, Read] = queryId(id).result

    def readAll: FixedSqlStreamingAction[Seq[PopulationDto], PopulationDto, Read] = populations.result

    def delete(id: String): FixedSqlAction[Int, NoStream, Write] = queryId(id).delete
  }

}

