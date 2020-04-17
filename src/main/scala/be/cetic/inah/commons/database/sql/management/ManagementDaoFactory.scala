package be.cetic.inah.commons.database.sql.management

import be.cetic.inah.commons.database.sql.DaoFactory
import be.cetic.inah.commons.database.sql.access_control.model.TokensDtoMultiDb
import be.cetic.inah.commons.database.sql.management.model._
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContextExecutor

class ManagementDaoFactory(val driver: JdbcProfile, val dbProfile: Option[String] = None)(implicit val dispatcher: ExecutionContextExecutor)
  extends DaoFactory with TokensDtoMultiDb with DatasourcesDtoMultiDb with ProjectsDtoMultiDb with ProjectDatasourcesDtoMultiDb with ProjectResourcesDtoMultiDb
    with ResourcesDtoMultiDb with UsersDtoMultiDb with ProjectUsersDtoMultiDb {

  import driver.api._

  def schemas: driver.DDL =
    DatasourceDao.elements.schema ++
      ProjectDao.projects.schema ++
      ResourceDao.resources.schema ++
      ProjectDatasourceDao.projectDatasources.schema ++
      ProjectResourceDao.projectResources.schema ++
      UserDao.users.schema ++
      ProjectUserDao.projectUsers.schema ++
      TokensDao.tokens.schema

}
