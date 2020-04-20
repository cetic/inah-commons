package be.cetic.inah.commons.database.sql.population.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import be.cetic.inah.commons.database.sql.population.PopulationResource
import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import slick.dbio.Effect.{Read, Write}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.ExecutionContextExecutor

case class PseudoDto(populationId: String, personIid: String, personPseudo: String) extends PopulationResource


trait PseudoDtoMultiDb extends DriverComponent with CoversDtoMultiDb with PopulationsDtoMultiDb {

  import driver.api._

  class PseudosDto(tag: Tag) extends Table[PseudoDto](tag, SchemaNames.populationSchemaName, "pseudo") {

    def populationId = column[String]("population_id")//, O.PrimaryKey)

    def personIid = column[String]("person_iid")//, O.PrimaryKey)

    def pk = primaryKey("pseudo_pk", (populationId, personIid))

    def personPseudo = column[String]("person_pseudo")

    def populationForeignKey = foreignKey("pseudo_population_fk", populationId, PopulationDao.populations)(_.id)

    def * = (populationId, personIid,personPseudo) <> (PseudoDto.tupled, PseudoDto.unapply)
  }


  implicit val dispatcher: ExecutionContextExecutor

  implicit object PseudoDao extends Dao[PseudoDto, (String, String)] {
    override val thisDriver = driver
    val pseudos = TableQuery[PseudosDto]

    def create(element: PseudoDto) = {
      (pseudos += element).map(_ => element)
    }

    def bulkCreate(elements: PseudoDto*) = {
      (pseudos ++= elements).map(_ => elements)
    }

    def update(element: PseudoDto): DBIOAction[PseudoDto, NoStream, Write] = {
      pseudos.insertOrUpdate(element).map { _ => element }
    }

    private def queryId(populationId: String, personPseudo: String) =
      pseudos.filter(p => p.populationId === populationId && p.personPseudo === personPseudo)

    def read(populationId: String, personPseudo: String): FixedSqlStreamingAction[Seq[PseudoDto], PseudoDto, Read] = queryId(populationId, personPseudo).result
    def read(pK: (String,String))=read(pK._1, pK._2)
    def readAll: FixedSqlStreamingAction[Seq[PseudoDto], PseudoDto, Read] = pseudos.result

    def delete(populationId: String, personPseudo: String): FixedSqlAction[Int, NoStream, Write] = queryId(populationId, personPseudo).delete
    def delete(pK:(String, String)) = delete(pK._1, pK._2)
  }

}

