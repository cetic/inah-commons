package be.cetic.inah.commons.database.sql.access_control

import be.cetic.inah.commons.database.sql.DaoFactory
import be.cetic.inah.commons.database.sql.access_control.model._
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContextExecutor

class AccessControlDaoFactory(val driver: JdbcProfile, val dbProfile: Option[String] = None)(implicit val dispatcher: ExecutionContextExecutor)
  extends DaoFactory with PoliciesDtoMultiDb with RightPoliciesDtoMultiDb with RightsDtoMultiDb
    with TokenAccessesDtoMultiDb with TokenContextsDtoMultiDb with TokensDtoMultiDb with ContextsDtoMultiDb
    with RoutesDtoMultiDb with RulesDtoMultiDb with ServicesDtoMultiDb {

  import driver.api._

  def schemas: driver.DDL = AccessPoliciesDao.policies.schema ++
    RightPoliciesDao.rightPolicies.schema ++
    RightsDao.rights.schema ++
    TokenRightsDao.tokenRights.schema ++
    TokenContextsDao.tokenContexts.schema ++
    TokensDao.tokens.schema ++
    ContextsDao.contexts.schema ++
    RoutesDao.routes.schema ++
    RulesDao.rules.schema ++
    ServicesDao.services.schema


}
