package be.cetic.inah.commons.database.sql.management

import be.cetic.inah.commons.database.sql.Dao
import be.cetic.inah.commons.database.sql.access_control.model.TokensDtoMultiDb
import be.cetic.inah.commons.database.sql.management.model.{DatasourcesDtoMultiDb, ProjectDatasourcesDtoMultiDb, ProjectResourcesDtoMultiDb, ProjectUsersDtoMultiDb, ProjectsDtoMultiDb, ResourcesDtoMultiDb, UsersDtoMultiDb}
import slick.dbio.Effect.{Read, Write}
import slick.jdbc.JdbcProfile
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

class ManagementDaoFactory(val driver: JdbcProfile, dbProfile: Option[String] = None, val schemaName: Option[String] = None)(implicit val dispatcher: ExecutionContextExecutor)
  extends TokensDtoMultiDb with DatasourcesDtoMultiDb with ProjectsDtoMultiDb with ProjectDatasourcesDtoMultiDb with ProjectResourcesDtoMultiDb
    with ResourcesDtoMultiDb with UsersDtoMultiDb with ProjectUsersDtoMultiDb {

  import driver.api._

  val db = dbProfile.map(prof => Database.forConfig(prof))
    .getOrElse {
      val pgUrl = sys.env("PG_URL")
      val pgUser = sys.env.getOrElse("PG_USER", "postgres")
      val pgPwd = sys.env.getOrElse("PG_PWD", "postgres")
      Database.forURL(s"jdbc:postgresql://${pgUrl}/postgres", user = pgUser, password = pgPwd)
    }


  //val dbTestSqlite = Database.forURL("jdbc:sqlite:test.db", driver = "org.sqlite.JDBC")


  def createSchema(schema: String) = sqlu"""CREATE SCHEMA IF NOT EXISTS "#$schema";"""

  def schemas =
    DatasourceDao.elements.schema ++
      ProjectDao.projects.schema ++
      ResourceDao.resources.schema ++
      ProjectDatasourceDao.projectDatasources.schema ++
      ProjectResourceDao.projectResources.schema ++
      UserDao.users.schema ++
      ProjectUserDao.projectUsers.schema ++
      TokensDao.tokens.schema

  def createTables = schemas.create

  def dropTables = schemas.drop

  val setupActions = Seq(schemaName.map(createSchema), Some(createTables)).filter(_.isDefined).map(_.get)

  def setup = DBIO.seq(setupActions: _*)

  def create[T, PK](pkType: Class[PK], element: T)(implicit dao: Dao[T, PK]): dao.thisDriver.api.DBIOAction[T, dao.thisDriver.api.NoStream, Write] = dao.create(element)

  def update[T, PK](pkType: Class[PK], element: T)(implicit dao: Dao[T, PK]): dao.thisDriver.api.DBIOAction[T, dao.thisDriver.api.NoStream, Write] = dao.update(element)

  def read[T, PK](dtoType: Class[T], id: PK)(implicit dao: Dao[T, PK]): FixedSqlStreamingAction[Seq[T], T, Read] = dao.read(id)

  def delete[T, PK](dtoType: Class[T], id: PK)(implicit dao: Dao[T, PK]): FixedSqlAction[Int, dao.thisDriver.api.NoStream, Write] = dao.delete(id)

  def readAll[T, PK](dtoType: Class[T], pkType: Class[PK])(implicit dao: Dao[T, PK]): FixedSqlStreamingAction[Seq[T], T, Read] = dao.readAll

}
