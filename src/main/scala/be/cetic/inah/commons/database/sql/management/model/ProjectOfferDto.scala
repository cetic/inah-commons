package be.cetic.inah.commons.database.sql.management.model

import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import be.cetic.inah.commons.database.sql.management.ManagementResource
import slick.dbio.Effect.Write
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class ProjectOfferDto (projectId : String, offerId : Int) extends ManagementResource
trait ProjectOffersDtoMultiDb extends ProjectsDtoMultiDb with OffersDtoMultiDb with DriverComponent {
  import driver.api._

  class ProjectOffersDto (tag : Tag ) extends Table[ProjectOfferDto] (tag, SchemaNames.managementSchemaName, "project_offer") {

    def projectId  = column[String]("project_id")
    def offerId = column[Int]("offer_id")
    def pk = primaryKey("project_offer_pk", (projectId, offerId))
    def project = foreignKey("project_project_fk", projectId, ProjectDao.projects)(_.id)
    def offer = foreignKey("project_offer_fk", offerId, OfferDao.offers)(_.id)
    def * = (projectId, offerId) <> (ProjectOfferDto.tupled, ProjectOfferDto.unapply)
  }

  implicit val dispatcher: ExecutionContextExecutor

  implicit object ProjectOfferDao extends Dao[ProjectOfferDto, (String, Int)] {
    val thisDriver = driver
    val projectOffers = TableQuery[ProjectOffersDto]

    private def queryId(id: (String, Int)) = projectOffers.filter(p => p.projectId === id._1 && p.offerId === id._2)

    def create(element: ProjectOfferDto): DBIOAction[ProjectOfferDto, NoStream, Write] = {
      (projectOffers+=element).map(_=>element)
    }

    def update(element: ProjectOfferDto): DBIOAction[ProjectOfferDto, NoStream, Write] = {

      projectOffers.insertOrUpdate(element).map(_=>element)
    }

     def read(id : (String, Int)): FixedSqlStreamingAction[Seq[ProjectOfferDto], ProjectOfferDto, Effect.Read] = queryId(id).result

    def readAll =projectOffers.result

    def delete(id: (String, Int)) = queryId(id).delete
  }




}
