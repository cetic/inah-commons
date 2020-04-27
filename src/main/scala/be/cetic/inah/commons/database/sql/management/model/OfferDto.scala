package be.cetic.inah.commons.database.sql.management.model

import java.sql.{Blob, Driver}

import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import be.cetic.inah.commons.database.sql.management.ManagementResource
import slick.dbio.Effect.Read
import slick.lifted.{Isomorphism, MappedScalaProductShape, Shape, ShapeLevel}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor
import scala.reflect.ClassTag


case class OfferDto (id : Option[Int], name : String, requiredSections : Seq[String], requiredUploads :Blob) extends ManagementResource

trait OffersDtoMultiDb extends DriverComponent {
  import driver.api._

  class OffersDto (tag : Tag ) extends Table[OfferDto] (tag, SchemaNames.managementSchemaName, "offer"){

    def id = column[Option[Int]]("id", O.PrimaryKey)
    def name = column[String]("name")
    //
    def requiredSections = column[String]("required_sections")
    def requiredUploads = column[Blob]("required_uploads")

    def myTupled = (x: (Option[Int], String, String, Blob)) => OfferDto(x._1, x._2, x._3.split(";"), x._4)
    def myUnapply : OfferDto => Option[(Option[Int], String, String, Blob)]= (o: OfferDto)=> Some(o.id, o.name, o.requiredSections.mkString(";"), o.requiredUploads)
    def * = (id, name, requiredSections, requiredUploads) <> (myTupled, myUnapply)
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