package be.cetic.inah.commons.database.sql.management.model

import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import be.cetic.inah.commons.database.sql.management.ManagementResource
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class OfferAppendixPatternDto (offerId : Option[Int], descriptionId : Option[Int] ) extends ManagementResource

trait OffersAppendicesPatternDtoMultiDb extends DriverComponent with ServiceOffersDtoMultiDb with PatternDescriptionsDtoMultiDb {
  import driver.api._
  class OffersAppendicesPatternDto (tag: Tag) extends Table[OfferAppendixPatternDto] (tag, SchemaNames.managementSchemaName, "offer_appendix_pattern") {

    def offerId = column[Option[Int]]("offer_id")
    def descriptionId = column[Option[Int]]("description_id")
    def pk = primaryKey("offer_appendix_pk",(offerId, descriptionId))
    def offer = foreignKey("offer_appendix_offer_fk", offerId, ServiceOfferDao.serviceOffers)(_.id)
    def description = foreignKey("offer_appendix_description_fk", descriptionId, PatternDescriptionDao.patternDescriptions)(_.id)
    def * = (offerId, descriptionId) <> (OfferAppendixPatternDto.tupled, OfferAppendixPatternDto.unapply)
  }
  implicit val dispatcher: ExecutionContextExecutor

  implicit object OfferAppendixPatternDao extends Dao [OfferAppendixPatternDto, (Int, Int)] {
    val thisDriver = driver
    val offersAppendices = TableQuery[OffersAppendicesPatternDto]
    private def queryById (id: (Int, Int)) = offersAppendices.filter(o=>o.offerId===id._1&&o.descriptionId===id._2)
    def create(element: OfferAppendixPatternDto): DBIOAction[OfferAppendixPatternDto, NoStream, Effect.Write] = {

      (offersAppendices+=element).map(_=>element)
    }

    def update(element: OfferAppendixPatternDto): DBIOAction[OfferAppendixPatternDto, NoStream, Effect.Write] = {

      (offersAppendices.insertOrUpdate(element)).map(_=>element)

    }

    def read(id: (Int, Int)): FixedSqlStreamingAction[Seq[OfferAppendixPatternDto], OfferAppendixPatternDto, Effect.Read] = queryById(id).result

   def readAll = offersAppendices.result

    def delete(id: (Int, Int)) = queryById(id).delete
  }


}