package be.cetic.inah.commons.database.sql.access_control.model

import be.cetic.inah.commons.database.sql.access_control.AccessControl
import be.cetic.inah.commons.database.sql.{DriverComponent, DtoWithId}
import slick.dbio.Effect.{Read, Write}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor


case class RouteDto(id: Option[Int], endpoint: String, serviceId: Option[Int], method: String) extends DtoWithId[Int]

trait RoutesDtoMultiDb extends DriverComponent with ServicesDtoMultiDb with AccessControl {

  import driver.api._

  class RoutesDto(tag: Tag) extends Table[RouteDto](tag, schemaName.orElse(accessControlSchemaName), "routes") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def serviceId = column[Option[Int]]("service_id")

    def endpoint = column[String]("endpoint")

    def method = column[String]("method")

    def * = (id.?, endpoint, serviceId, method) <> (RouteDto.tupled, RouteDto.unapply)

    def service = foreignKey("route_service_fk", serviceId, ServicesDao.services)(_.id)
  }


  implicit val dispatcher: ExecutionContextExecutor

  object RoutesDao {

    val routes = TableQuery[RoutesDto]

    private val aAutoInc = routes returning routes.map(_.id)

    def create(element: RouteDto): DBIOAction[RouteDto, NoStream, Write] = {
      (aAutoInc += element).map(id => element.copy(id = Some(id)))
    }

    def update(element: RouteDto): DBIOAction[Any, NoStream, Write] = aAutoInc.insertOrUpdate(element)
      .map { maybeId =>
        maybeId.map { id =>
          element.copy(id = Some(id))
        }
          .getOrElse(element)
      }

    private def queryId(id: Int) = routes.filter(_.id === id)

    def read(id: Int): FixedSqlStreamingAction[Seq[RouteDto], RouteDto, Read] = queryId(id).result

    def readAll: FixedSqlStreamingAction[Seq[RouteDto], RouteDto, Read] = routes.result

    def delete(id: Int): FixedSqlAction[Int, NoStream, Write] = queryId(id).delete
  }

}
