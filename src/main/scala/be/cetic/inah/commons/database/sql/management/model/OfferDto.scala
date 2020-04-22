package be.cetic.inah.commons.database.sql.management.model

import java.sql.{Blob, Driver}

import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import be.cetic.inah.commons.database.sql.management.ManagementResource
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
    override def * = ???
  }

  implicit val dispatcher: ExecutionContextExecutor

  implicit object OfferDao extends Dao[OfferDto, Int]{

    val thisDriver = driver
    val offers = TableQuery[OffersDto]

    def queryById (id: Int ) = ???
    override def create(element: OfferDto): _root_.be.cetic.inah.commons.database.sql.management.model.OffersDtoMultiDb.OfferDao.thisDriver.api.DBIOAction[OfferDto, _root_.be.cetic.inah.commons.database.sql.management.model.OffersDtoMultiDb.OfferDao.thisDriver.api.NoStream, Effect.Write] = ???

    override def update(element: OfferDto): _root_.be.cetic.inah.commons.database.sql.management.model.OffersDtoMultiDb.OfferDao.thisDriver.api.DBIOAction[OfferDto, _root_.be.cetic.inah.commons.database.sql.management.model.OffersDtoMultiDb.OfferDao.thisDriver.api.NoStream, Effect.Write] = ???

    override def read(id: Int): FixedSqlStreamingAction[Seq[OfferDto], OfferDto, Effect.Read] = ???

    override def readAll = ???

    override def delete(id: Int): FixedSqlAction[Int, _root_.be.cetic.inah.commons.database.sql.management.model.OffersDtoMultiDb.OfferDao.thisDriver.api.NoStream, Effect.Write] = ???
  }
}