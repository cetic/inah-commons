package be.cetic.inah.commons.database.sql.temp_objects

import akka.io.Tcp.Write
import be.cetic.inah.commons.database.sql._
import be.cetic.inah.commons.database.sql.management.model.{DatasourcesDtoMultiDb, ResourcesDtoMultiDb}
import be.cetic.inah.commons.database.sql.population.PopulationResource
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class DatasetSyncDto(id: Option[Int], resourceId: Int, datasourceId: Int, received: Boolean) extends Dto

trait DatasetSyncsDtoMultiDb extends DriverComponent with ResourcesDtoMultiDb with DatasourcesDtoMultiDb {

  import driver.api._

  class DatasetsDto(tag: Tag) extends Table[DatasetSyncDto](tag, SchemaNames.tempObjectsSchemaName, "dataset_sync") {

    def id = column[Int]("id", O.PrimaryKey)

    def resourceId = column[Int]("resource_id")

    def datasourceId = column[Int]("datasource_id")

    def received = column[Boolean]("population_id")

    def resource = foreignKey("ds_sync_resource_fk", resourceId, ResourceDao.resources)(_.id)

    def datasource = foreignKey("ds_sync_source_fk", datasourceId, DatasourceDao.elements)(_.id)


    def * = (id.?, resourceId, datasourceId, received) <> (DatasetSyncDto.tupled, DatasetSyncDto.unapply)
  }


  implicit val dispatcher: ExecutionContextExecutor


  implicit object DatasetSyncDao extends Dao[DatasetSyncDto, Int] {

    override val thisDriver = driver
    val datasetSyncs = TableQuery[DatasetsDto]

    val datasetSyncsAutoInc = datasetSyncs returning datasetSyncs.map(_.id)


    def create(element: DatasetSyncDto) = {
      (datasetSyncsAutoInc += element).map(_ => element)
    }

    def update(element: DatasetSyncDto): DBIOAction[DatasetSyncDto, NoStream, Effect.Write] = {

      datasetSyncsAutoInc.insertOrUpdate(element).map { _ => element }
    }

    private def queryById(id: Int) = datasetSyncs.filter(_.id === id)

    def read(id: Int): FixedSqlStreamingAction[Seq[DatasetSyncDto], DatasetSyncDto, Effect.Read] = queryById(id).result


    def readAll: FixedSqlStreamingAction[Seq[DatasetSyncDto], DatasetSyncDto, Effect.Read] = datasetSyncs.result

    def delete(id: Int): FixedSqlAction[Int, NoStream, Effect.Write] = queryById(id).delete
  }

}
