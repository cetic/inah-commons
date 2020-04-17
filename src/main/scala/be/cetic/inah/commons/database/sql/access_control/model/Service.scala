package be.cetic.inah.commons.database.sql.access_control.model

import be.cetic.inah.commons.database.sql.access_control.AccessControlResource
import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, DtoWithId, SchemaNames}
import slick.dbio.Effect.{Read, Write}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor


case class ServiceDto(id: Option[Int], name: String, description: String, baseUrl: String, domain: String) extends DtoWithId[Int] with AccessControlResource


trait ServicesDtoMultiDb extends DriverComponent {

  import driver.api._

  implicit val dispatcher: ExecutionContextExecutor

  class ServicesDto(tag: Tag) extends Table[ServiceDto](tag,  SchemaNames.accessControlSchemaName, "services") {
    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)

    def name = column[String]("name")

    def description = column[String]("description")

    def baseUrl = column[String]("base_url")

    def domain = column[String]("domain")

    def * = (id.?, name, description, baseUrl, domain) <> (ServiceDto.tupled, ServiceDto.unapply)
  }

  implicit object ServicesDao extends Dao[ServiceDto, Int] {
    override val thisDriver = driver
    val services = TableQuery[ServicesDto]

    private val serviceAutoInc = services returning services.map(_.id)

    def create(service: ServiceDto): DBIOAction[ServiceDto, NoStream, Write] = {
      (serviceAutoInc += service).map(id => service.copy(id = Some(id)))
    }

    def update(service: ServiceDto): DBIOAction[ServiceDto, NoStream, Write] = serviceAutoInc.insertOrUpdate(service)
      .map { maybeId =>
        maybeId.map { id =>
          service.copy(id = Some(id))
        }
          .getOrElse(service)
      }

    private def queryId(id: Int) = services.filter(_.id === id)

    def read(id: Int): FixedSqlStreamingAction[Seq[ServiceDto], ServiceDto, Read] = queryId(id).result

    def readAll: FixedSqlStreamingAction[Seq[ServiceDto], ServiceDto, Read] = services.result

    def delete(id: Int): FixedSqlAction[Int, NoStream, Write] = queryId(id).delete
  }

}
