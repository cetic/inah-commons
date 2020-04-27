package be.cetic.inah.commons.database.sql.management.model

import java.sql.{Blob, Driver}

import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import be.cetic.inah.commons.database.sql.management.ManagementResource
import slick.dbio.Effect.Read
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor


case class OfferDto (id : Int, name : String, requiredSections : List[String], requiredUploads : List[Blob]) extends ManagementResource

trait OffersDtoMultiDb extends DriverComponent {

  import driver.api._

  class OffersDto (tag : Tag ) extends Table[OfferDto] (tag, SchemaNames.managementSchemaName, "offer"){

    def id = column[Int]("id", O.PrimaryKey)
    def name = column[String]("name")
    def requiredSections = column[List[String]]("required_sections")
    def requiredUploads = column[List[Blob]]("required_uploads")

    def * = (id, name, requiredSections, requiredUploads) <> (OfferDto.tupled, OfferDto.unapply)
  }

  implicit val dispatcher: ExecutionContextExecutor

  implicit object OfferDao extends Dao[OfferDto, Int]{

    val thisDriver = driver
    val offers = TableQuery[OffersDto]

    private def queryById (id: Int ) = offers.filter(o => o.id===id)

    def create(element: OfferDto): DBIOAction[OfferDto, NoStream, Effect.Write] = {


      (offers+=element).map(_=>element)
    }

    def update(element: OfferDto): DBIOAction[OfferDto, NoStream, Effect.Write] = {

      offers.insertOrUpdate(element).map{_=>element}
    }

    def read(id: Int): FixedSqlStreamingAction[Seq[OfferDto], OfferDto, Effect.Read] = queryById(id).result

    def readAll : FixedSqlStreamingAction[Seq[OfferDto], OfferDto, Read] = offers.result

    def delete(id: Int) = queryById(id ).delete
  }
}