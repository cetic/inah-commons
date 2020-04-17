package be.cetic.inah.commons.database.sql.population

import be.cetic.inah.commons.database.sql.DaoFactory
import be.cetic.inah.commons.database.sql.population.model._
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContextExecutor

class PopulationDaoFactory(val driver: JdbcProfile, val dbProfile: Option[String] = None)(implicit val dispatcher: ExecutionContextExecutor)
  extends DaoFactory with  PopulationsDtoMultiDb with TargetsDtoMultiDb with CoversDtoMultiDb
    with PersonCoversDtoMultiDb with PersonPopulationDtoMultiDb with PseudoDtoMultiDb{

  import driver.api._

  def schemas: driver.DDL =  PopulationDao.populations.schema ++
    TargetDao.targets.schema ++
    CoverDao.covers.schema ++
    PersonCoverDao.personCovers.schema ++
    PersonPopulationDao.personPopulations.schema ++
    PseudoDao.pseudos.schema


}
