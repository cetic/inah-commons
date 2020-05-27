package be.cetic.inah.commons.database.sql.population.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import be.cetic.inah.commons.database.sql.population.PopulationResource
import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, DtoWithProvidedId, SchemaNames}
import slick.dbio.Effect.{Read, Write}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.ExecutionContextExecutor

case class PersonPopulationId(populationId: String, personIid: String) extends PopulationResource

case class PersonPopulationDto(id: PersonPopulationId, from: Long, to: Long, `type`: String)
  extends DtoWithProvidedId[PersonPopulationId] with PopulationResource


trait PersonPopulationDtoMultiDb extends DriverComponent with PopulationsDtoMultiDb {

  import driver.api._

  class PersonPopulationsDto(tag: Tag) extends Table[PersonPopulationDto](tag, SchemaNames.populationSchemaName, "person_population") {

    def populationId = column[String]("population_id")//, O.PrimaryKey)

    def population = foreignKey("person_population_fk", populationId, PopulationDao.populations)(_.id)

    def personIid = column[String]("person_iid")//, O.PrimaryKey)

    def pk = primaryKey("person_population_pk", (populationId, personIid))
    def from = column[Long]("from")

    def to = column[Long]("to")

    def `type` = column[String]("type")

    def coverTupled =
      (x: (String, String, Long, Long, String)) => PersonPopulationDto(PersonPopulationId(x._1, x._2), x._3, x._4, x._5)

    def coverUnapply = (p: PersonPopulationDto) => Some((p.id.populationId, p.id.personIid, p.from, p.to, p.`type`))

    def * = (populationId, personIid, from, to, `type`) <> (coverTupled, coverUnapply)
  }


  implicit val dispatcher: ExecutionContextExecutor

  implicit object PersonPopulationDao extends Dao[PersonPopulationDto,PersonPopulationId]{

    override val thisDriver = driver
    val personPopulations = TableQuery[PersonPopulationsDto]

    def create(element: PersonPopulationDto): DBIOAction[PersonPopulationDto, NoStream, Write] = {
      (personPopulations += element).map(_ => element)
    }

    def bulkCreate(elements: PersonPopulationDto*): DBIOAction[Seq[PersonPopulationDto], NoStream, Write] = (personPopulations ++= elements).map(_ => elements)


    def update(element: PersonPopulationDto): DBIOAction[PersonPopulationDto, NoStream, Write] = {
      personPopulations.insertOrUpdate(element).map { _ => element }
    }

    private def queryId(id: PersonPopulationId) = personPopulations.filter(pc => pc.populationId === id.populationId && pc.personIid === id.personIid)

    def read(id: PersonPopulationId): FixedSqlStreamingAction[Seq[PersonPopulationDto], PersonPopulationDto, Read] = queryId(id).result

    def readAll: FixedSqlStreamingAction[Seq[PersonPopulationDto], PersonPopulationDto, Read] = personPopulations.result

    def delete(id: PersonPopulationId): FixedSqlAction[Int, NoStream, Write] = queryId(id).delete
  }

}

