package be.cetic.inah.commons.database.sql.access_control.model

import be.cetic.inah.commons.database.sql.access_control.AccessControl
import be.cetic.inah.commons.database.sql.{DriverComponent, DtoWithId}
import slick.dbio.Effect.{Read, Write}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class DataDto(id: Option[Int], key: String, value: String) extends DtoWithId[Int]

trait ContextsDtoMultiDb extends DriverComponent with AccessControl {

  import driver.api._

  class ContextsDto(tag: Tag) extends Table[DataDto](tag, schemaName.orElse(accessControlSchemaName), "contexts") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def key = column[String]("key")

    def value = column[String]("value")

    def * = (id.?, key, value) <> (DataDto.tupled, DataDto.unapply)
  }


  implicit val dispatcher: ExecutionContextExecutor

  object ContextsDao {

    val contexts = TableQuery[ContextsDto]

    private val aAutoInc = contexts returning contexts.map(_.id)

    def create(element: DataDto): DBIOAction[DataDto, NoStream, Write] = {
      (aAutoInc += element).map(id => element.copy(id = Some(id)))
    }

    def update(element: DataDto): DBIOAction[Any, NoStream, Write] = aAutoInc.insertOrUpdate(element)
      .map { maybeId =>
        maybeId.map { id =>
          element.copy(id = Some(id))
        }
          .getOrElse(element)
      }

    private def queryId(id: Int) = contexts.filter(_.id === id)

    def read(id: Int): FixedSqlStreamingAction[Seq[DataDto], DataDto, Read] = queryId(id).result

    def readAll: FixedSqlStreamingAction[Seq[DataDto], DataDto, Read] = contexts.result

    def delete(id: Int): FixedSqlAction[Int, NoStream, Write] = queryId(id).delete

  }

}