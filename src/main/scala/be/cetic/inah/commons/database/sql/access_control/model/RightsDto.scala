package be.cetic.inah.commons.database.sql.access_control.model

import be.cetic.inah.commons.database.sql.access_control.AccessControlResource
import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, DtoWithId, SchemaNames}
import slick.dbio.Effect.{Read, Write}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor


case class RightDto(id: Option[Int], name: String, description: String, required: Boolean) extends DtoWithId[Int] with AccessControlResource

trait RightsDtoMultiDb extends DriverComponent  {

  import driver.api._

  class RightsDto(tag: Tag) extends Table[RightDto](tag,  SchemaNames.accessControlSchemaName, "rights") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def description = column[String]("description")

    def required = column[Boolean]("required")

    def * = (id.?, name, description, required) <> (RightDto.tupled, RightDto.unapply)
  }


  implicit val dispatcher: ExecutionContextExecutor

  implicit object RightsDao extends Dao[RightDto, Int] {
    override val thisDriver = driver
    val rights = TableQuery[RightsDto]

    private val aAutoInc = rights returning rights.map(_.id)

    def create(element: RightDto): DBIOAction[RightDto, NoStream, Write] = {
      (aAutoInc += element).map(id => element.copy(id = Some(id)))
    }

    def update(element: RightDto): DBIOAction[RightDto, NoStream, Write] = aAutoInc.insertOrUpdate(element)
      .map { maybeId =>
        maybeId.map { id =>
          element.copy(id = Some(id))
        }
          .getOrElse(element)
      }

    private def queryId(id: Int) = rights.filter(_.id === id)

    def read(id: Int): FixedSqlStreamingAction[Seq[RightDto], RightDto, Read] = queryId(id).result

    def readAll: FixedSqlStreamingAction[Seq[RightDto], RightDto, Read] = rights.result

    def delete(id: Int): FixedSqlAction[Int, NoStream, Write] = queryId(id).delete
  }

}