package be.cetic.inah.commons.database.sql.access_control

import be.cetic.inah.commons.database.sql.access_control.model.{ContextsDtoMultiDb, PoliciesDtoMultiDb, RightPoliciesDtoMultiDb, RightsDtoMultiDb, RoutesDtoMultiDb, RulesDtoMultiDb, ServicesDtoMultiDb, TokenAccessesDtoMultiDb, TokenContextsDtoMultiDb, TokensDtoMultiDb}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContextExecutor

class AccessControlDaoFactory(val driver: JdbcProfile, dbProfile: Option[String] = None, val schemaName: Option[String] = None)(implicit val dispatcher: ExecutionContextExecutor) extends PoliciesDtoMultiDb with RightPoliciesDtoMultiDb with RightsDtoMultiDb
  with TokenAccessesDtoMultiDb with TokenContextsDtoMultiDb with TokensDtoMultiDb with ContextsDtoMultiDb
  with RoutesDtoMultiDb  with RulesDtoMultiDb with ServicesDtoMultiDb {

  import driver.api._

  val db = dbProfile.map(prof => Database.forConfig(prof))
    .getOrElse {
      val pgUrl = sys.env("PG_URL")
      val pgUser = sys.env.getOrElse("PG_USER", "postgres")
      val pgPwd = sys.env.getOrElse("PG_PWD", "postgres")
      Database.forURL(s"jdbc:postgresql://${pgUrl}/postgres", user = pgUser, password = pgPwd)
    }


  def createSchema(schema: String) = sqlu"""CREATE SCHEMA IF NOT EXISTS "#$schema";"""

  def schemas = AccessPoliciesDao.policies.schema ++
    RightPoliciesDao.rightPolicies.schema ++
    RightsDao.rights.schema ++
    TokenRightsDao.tokenRights.schema ++
    TokenContextsDao.tokenContexts.schema ++
    TokensDao.tokens.schema ++
    ContextsDao.contexts.schema ++
    RoutesDao.routes.schema ++
    RulesDao.rules.schema ++
    ServicesDao.services.schema

  def createTables = schemas.create

  def dropTables = schemas.drop

  val setupActions = Seq(schemaName.map(createSchema), Some(createTables)).filter(_.isDefined).map(_.get)

  def setup = DBIO.seq(setupActions: _*)


}
