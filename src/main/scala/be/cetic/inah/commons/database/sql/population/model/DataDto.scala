package be.cetic.inah.commons.database.sql.population.model

import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, DtoWithProvidedId, SchemaNames}
import be.cetic.inah.commons.database.sql.population.PopulationResource
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class DataPersonId (datasetId : Int, personId: String ) extends PopulationResource

case class DataDto (id : DataPersonId, ts: Long, entityName : String, entityLabel : String , value: String, unit : String )
  extends PopulationResource with DtoWithProvidedId[DataPersonId]


trait DataDtoMultiDb extends DriverComponent with PersonPopulationDtoMultiDb with DatasetDtoMultiDb {

  import driver.api._

  class DatasDto (tag : Tag ) extends Table[DataDto] (tag, SchemaNames.populationSchemaName, "data"){

    def datasetId = column[Int]("dataset_id")
    def personId = column[String]("person_id")

    def dataset = foreignKey("data_dataset_fk", datasetId, DatasetDao.datasets)(_.id)
    def person = foreignKey("data_person_fk", personId, PersonPopulationDao.personPopulations)(_.personIid)

    def pk = primaryKey("data_pk", (datasetId, personId))

    def ts = column[Long]("ts")
    def entityName = column[String]("entity_name")
    def entityLabel = column[String]("entity_label")
    def value = column[String]("value")
    def unit = column[String]("unit")

    def dataTupled =
      (x : (Int, String, Long, String, String, String, String)) => DataDto(DataPersonId(x._1, x._2), x._3, x._4, x._5, x._6, x._7)

    def dataUnapply = (d : DataDto) => Some((d.id.datasetId, d.id.personId, d.ts, d.entityName, d.entityLabel, d.value, d.unit))
    def * = (datasetId, personId, ts, entityName, entityLabel, value, unit) <> (dataTupled, dataUnapply)

  }

  implicit val dispatcher: ExecutionContextExecutor

  implicit object DataDao extends Dao[DataDto, DataPersonId] {
    override val thisDriver = driver
    val datas = TableQuery[DatasDto]

    private def queryById (id : DataPersonId) = datas.filter(ds => ds.datasetId === id.datasetId && ds.personId === id.personId )

    def create(element: DataDto) = {

      (datas+=element).map(_=>element)
    }

    def update(element: DataDto): DBIOAction[DataDto, NoStream, Effect.Write] = {

      datas.insertOrUpdate(element).map(_=>element)
    }

   def read(id: DataPersonId): FixedSqlStreamingAction[Seq[DataDto], DataDto, Effect.Read] =queryById(id).result
    def readAll : FixedSqlStreamingAction[Seq[DataDto], DataDto, Effect.Read]  = datas.result

    def delete(id: DataPersonId): FixedSqlAction[Int, NoStream, Effect.Write] = queryById(id).delete
  }


}