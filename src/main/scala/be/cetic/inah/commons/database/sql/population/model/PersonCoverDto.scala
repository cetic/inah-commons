package be.cetic.inah.commons.database.sql.population.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import be.cetic.inah.commons.database.sql.population.PopulationResource
import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, DtoWithProvidedId, SchemaNames}
import slick.dbio.Effect.{Read, Write}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.ExecutionContextExecutor

case class PersonCoverId(coverId: Int, personIid: String)

case class PersonCoverDto(id: PersonCoverId, from: Long, to: Long, `type`: String) extends DtoWithProvidedId[PersonCoverId] with PopulationResource


trait PersonCoversDtoMultiDb extends DriverComponent with CoversDtoMultiDb {

  import driver.api._

  class PersonCoversDto(tag: Tag) extends Table[PersonCoverDto](tag, SchemaNames.populationSchemaName, "person_cover") {
    def coverId = column[Int]("cover_id") //, O.PrimaryKey)

    def cover = foreignKey("cover_fk", coverId, CoverDao.covers)(_.id)

    def personIid = column[String]("person_iid") //, O.PrimaryKey)

    def pk = primaryKey("id", (coverId, personIid))

    def from = column[Long]("from")

    def to = column[Long]("to")

    def `type` = column[String]("type")

    def coverTupled = (x: (Int, String, Long, Long, String)) => PersonCoverDto(PersonCoverId(x._1, x._2), x._3, x._4, x._5)

    def coverUnapply = (p: PersonCoverDto) => Some((p.id.coverId, p.id.personIid, p.from, p.to, p.`type`))

    def * = (coverId, personIid, from, to, `type`) <> (coverTupled, coverUnapply)
  }


  implicit val dispatcher: ExecutionContextExecutor

  implicit object PersonCoverDao extends Dao[PersonCoverDto, PersonCoverId] {
    override val thisDriver = driver
    val personCovers = TableQuery[PersonCoversDto]

    def create(element: PersonCoverDto) = {
      (personCovers += element).map(_ => element)
    }

    def bulkCreate(elements: PersonCoverDto*) = {
      (personCovers ++= elements).map(_ => elements)
    }

    def update(element: PersonCoverDto): DBIOAction[PersonCoverDto, NoStream, Write] = {
      personCovers.insertOrUpdate(element).map { _ => element }
    }

    private def queryId(id: PersonCoverId) = personCovers.filter(pc => pc.coverId === id.coverId && pc.personIid === id.personIid)

    def read(id: PersonCoverId): FixedSqlStreamingAction[Seq[PersonCoverDto], PersonCoverDto, Read] = queryId(id).result

    def readAll: FixedSqlStreamingAction[Seq[PersonCoverDto], PersonCoverDto, Read] = personCovers.result

    def delete(id: PersonCoverId): FixedSqlAction[Int, NoStream, Write] = queryId(id).delete
  }

}

