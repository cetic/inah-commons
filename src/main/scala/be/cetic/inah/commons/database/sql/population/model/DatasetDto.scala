package be.cetic.inah.commons.database.sql.population.model

import akka.io.Tcp.Write
import be.cetic.inah.commons.database.sql._
import be.cetic.inah.commons.database.sql.population.PopulationResource
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class DatasetDto (id : Int , description : String, status : String , populationId : String ) extends DtoWithProvidedId[Int] with PopulationResource

trait DatasetDtoMultiDb extends DriverComponent with PopulationsDtoMultiDb {

  import driver.api._

  class DatasetsDto (tag : Tag ) extends Table[DatasetDto] (tag, SchemaNames.populationSchemaName, "dataset") {

    def id = column[Int] ("id", O.PrimaryKey)
    def description = column[String]("description")
    def status = column[String]("status")
    def populationId = column[String]("population_id")
    def population = foreignKey("dataset_population_fk", populationId, PopulationDao.populations)(_.id)

    def * = (id, description, status, populationId) <> (DatasetDto.tupled, DatasetDto.unapply)
  }


  implicit val dispatcher: ExecutionContextExecutor


  implicit object DatasetDao extends Dao[DatasetDto, Int] {

    override val thisDriver = driver
    val datasets = TableQuery[DatasetsDto]

    def create(element: DatasetDto) = {
      (datasets +=element).map(_ => element)
    }

    def update(element: DatasetDto) : DBIOAction[DatasetDto, NoStream, Effect.Write] = {

      datasets.insertOrUpdate(element).map {_=>element}
    }

    private def queryById (id: Int) = datasets.filter(_.id === id)

    def read(id: Int): FixedSqlStreamingAction[Seq[DatasetDto], DatasetDto, Effect.Read] = queryById(id).result


    def readAll : FixedSqlStreamingAction[Seq[DatasetDto], DatasetDto, Effect.Read] = datasets.result

    def delete(id: Int): FixedSqlAction[Int,NoStream, Effect.Write] = queryById(id).delete
  }

}
