package be.cetic.inah.commons.database.sql.management.model

import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import be.cetic.inah.commons.database.sql.management.ManagementResource
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class OfferSectionPatternDto ( offerId : Int, sectionPatternId : Int ) extends ManagementResource

trait OfferSectionPatternsDtoMultiDb  extends DriverComponent with ServiceOffersDtoMultiDb with SectionPatternsDtoMultiDb  {
  import driver.api._

  class OfferSectionsPatternDto (tag: Tag) extends Table[OfferSectionPatternDto] (tag, SchemaNames.managementSchemaName, "offer_section_pattern") {

    def offerId = column[Int]("offer_id")
    def sectionPatternId = column[Int]("section_pattern_id")
    def offer = foreignKey("offer_section_pattern_offer_fk", offerId, ServiceOfferDao.serviceOffers)(_.id)
    def description = foreignKey("offer_section_pattern_section_fk",sectionPatternId,SectionPatternDao.sectionPatterns)(_.id)
    def * = ( offerId,sectionPatternId) <> (OfferSectionPatternDto.tupled, OfferSectionPatternDto.unapply)
  }

  implicit val dispatcher: ExecutionContextExecutor
  implicit object OfferSectionPatternDao extends Dao[OfferSectionPatternDto, (Int, Int)] {


    val thisDriver = driver
    val offerSections = TableQuery[OfferSectionsPatternDto]

    private def queryById (id: (Int, Int))= offerSections.filter(o => o.offerId === id._1 && o.sectionPatternId === id._2)
    def create(element: OfferSectionPatternDto): DBIOAction[OfferSectionPatternDto, NoStream, Effect.Write] = {
      (offerSections+=element).map(_=>element)
    }

    def update(element: OfferSectionPatternDto): DBIOAction[OfferSectionPatternDto, NoStream, Effect.Write] = {
      (offerSections.insertOrUpdate(element)).map(_=>element)
    }

    def read(id: (Int, Int)): FixedSqlStreamingAction[Seq[OfferSectionPatternDto], OfferSectionPatternDto, Effect.Read] = queryById(id).result

    def readAll = offerSections.result

    def delete(id: (Int, Int))= queryById(id).delete
  }


}
