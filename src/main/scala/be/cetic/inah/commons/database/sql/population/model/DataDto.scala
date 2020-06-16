package be.cetic.inah.commons.database.sql.population.model

import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, Dto, DtoWithProvidedId, SchemaNames}
import be.cetic.inah.commons.database.sql.population.PopulationResource
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class DataPersonId() extends PopulationResource

case class DataDto(id: Option[Int], datasetId: String, personId: String, ts: Long, entityName: String, entityLabel: String, entityId: String,
                   value: Option[String], unit: Option[String], extra: Option[String])
  extends PopulationResource with Dto


trait DataDtoMultiDb extends DriverComponent with PersonPopulationDtoMultiDb with DatasetsDtoMultiDb {

  import driver.api._

  class DatasDto(tag: Tag) extends Table[DataDto](tag, SchemaNames.populationSchemaName, "data") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def datasetId = column[String]("dataset_id")

    def personId = column[String]("person_id")

    def ts = column[Long]("ts")

    def entityName = column[String]("entity_name")

    def entityLabel = column[String]("entity_label")

    def entityIid = column[String]("entity_iid")

    def value = column[Option[String]]("value")

    def unit = column[Option[String]]("unit")

    def extra = column[Option[String]]("extra")

    def dataset = foreignKey("data_dataset_fk", datasetId, DatasetDao.datasets)(_.id)

    def datasetIndex = index("data_dataset_index", datasetId)

    def * = (id.?, datasetId, personId, ts, entityName, entityLabel, entityIid, value, unit, extra) <> (DataDto.tupled, DataDto.unapply)


  }

  implicit val dispatcher: ExecutionContextExecutor

  implicit object DataDao extends Dao[DataDto, Int] {
    override val thisDriver = driver
    val datas = TableQuery[DatasDto]
    val dataAutoInc = datas returning datas.map(_.id)

    private def queryById(id: Int) = datas.filter(ds => ds.id === id)

    def create(element: DataDto) = {
      (dataAutoInc += element).map(id => element.copy(id = Some(id)))
    }

    def update(element: DataDto): DBIOAction[DataDto, NoStream, Effect.Write] = {
      datas.insertOrUpdate(element).map(_ => element)
    }

    def read(id: Int): FixedSqlStreamingAction[Seq[DataDto], DataDto, Effect.Read] = queryById(id).result

    def readAll: FixedSqlStreamingAction[Seq[DataDto], DataDto, Effect.Read] = datas.result

    def delete(id: Int): FixedSqlAction[Int, NoStream, Effect.Write] = queryById(id).delete
  }


}