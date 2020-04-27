package be.cetic.inah.commons.database.sql.management.model

import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import be.cetic.inah.commons.database.sql.management.ManagementResource
import slick.dbio.Effect.Read
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}
import scala.concurrent.ExecutionContextExecutor


case class ServiceOfferDto(id : Option[Int], name : String) extends ManagementResource

trait ServiceOffersDtoMultiDb extends DriverComponent {
  import driver.api._

  class ServiceOffersDto (tag : Tag ) extends Table[ServiceOfferDto] (tag, SchemaNames.managementSchemaName, "service_offer"){

    def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def * = (id, name) <> (ServiceOfferDto.tupled, ServiceOfferDto.unapply)
  }

  implicit val dispatcher: ExecutionContextExecutor

  implicit object ServiceOfferDao extends Dao[ServiceOfferDto, Int]{

    val thisDriver = driver
    val serviceOffers = TableQuery[ServiceOffersDto]

    private def queryById (id: Int ) = serviceOffers.filter(o => o.id===id)

    def create(element: ServiceOfferDto): DBIOAction[ServiceOfferDto, NoStream, Effect.Write] = {


      (serviceOffers+=element).map(_=>element)
    }

    def update(element: ServiceOfferDto): DBIOAction[ServiceOfferDto, NoStream, Effect.Write] = {

      serviceOffers.insertOrUpdate(element).map{_=>element}
    }

    def read(id: Int): FixedSqlStreamingAction[Seq[ServiceOfferDto], ServiceOfferDto, Effect.Read] = queryById(id).result

    def readAll : FixedSqlStreamingAction[Seq[ServiceOfferDto], ServiceOfferDto, Read] = serviceOffers.result

    def delete(id: Int) = queryById(id ).delete
  }
}