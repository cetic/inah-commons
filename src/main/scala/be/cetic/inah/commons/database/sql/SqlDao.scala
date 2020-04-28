package be.cetic.inah.commons.database.sql

import be.cetic.inah.commons.database.sql.access_control.AccessControlDaoFactory
import be.cetic.inah.commons.database.sql.management.ManagementDaoFactory
import be.cetic.inah.commons.database.sql.population.PopulationDaoFactory
import slick.jdbc.JdbcProfile
import slick.sql.SqlAction

import scala.concurrent.ExecutionContextExecutor

object SqlDao {
  var daoFactory: Option[SqlDao] = None

  def dao: SqlDao = daoFactory.get

  def set(dao: SqlDao) = {
    daoFactory = Some(dao)
    dao
  }

  def set(driver: JdbcProfile, dbProfile: Option[String] = None)(implicit dispatcher: ExecutionContextExecutor) = {
    daoFactory = Some(new SqlDao(driver, dbProfile))
    dao
  }

}

class SqlDao(val driver: JdbcProfile, val dbProfile: Option[String] = None)(implicit val dispatcher: ExecutionContextExecutor)
  extends DaoFactory {

  import driver.api._

  val accessControlDao = new AccessControlDaoFactory(driver, dbProfile)
  val managementDao = new ManagementDaoFactory(driver, dbProfile)
  val populationDao = new PopulationDaoFactory(driver, dbProfile)

  val createSchemaStatements: Seq[SqlAction[Int, NoStream, Effect]] = SchemaNames.schemaNames.map(createSchema)

  override def schemas: driver.DDL = accessControlDao.schemas.asInstanceOf[driver.DDL] ++ managementDao.schemas.asInstanceOf[driver.DDL] ++
    populationDao.schemas.asInstanceOf[driver.DDL]


}