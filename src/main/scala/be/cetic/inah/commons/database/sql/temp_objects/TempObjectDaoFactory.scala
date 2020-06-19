package be.cetic.inah.commons.database.sql.temp_objects

import be.cetic.inah.commons.database.sql.DaoFactory
import be.cetic.inah.commons.database.sql.access_control.model.TokensDtoMultiDb
import be.cetic.inah.commons.database.sql.management.model._
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContextExecutor

class TempObjectDaoFactory(val driver: JdbcProfile, val dbProfile: Option[String] = None)(implicit val dispatcher: ExecutionContextExecutor)
  extends DaoFactory with TempDataDtoMultiDb with DatasetSyncsDtoMultiDb{

  import driver.api._

  def schemas: driver.DDL = TempDataDao.datas.schema ++ DatasetSyncDao.datasetSyncs.schema

}
