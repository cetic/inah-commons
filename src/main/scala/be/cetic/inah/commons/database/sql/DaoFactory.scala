package be.cetic.inah.commons.database.sql

import slick.dbio.Effect.{Read, Write}
import slick.jdbc.JdbcProfile
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

trait DaoFactory {
  val driver: JdbcProfile
  val dbProfile: Option[String]
  //todo : remove schemaName everywhere
  val schemaName: Option[String] = None

  implicit val dispatcher: ExecutionContextExecutor

  import driver.api._

  val db = dbProfile.map(prof => Database.forConfig(prof))
    .getOrElse {
      val pgUrl = sys.env("PG_URL")
      val pgUser = sys.env.getOrElse("PG_USER", "postgres")
      val pgPwd = sys.env.getOrElse("PG_PWD", "postgres")
      Database.forURL(s"jdbc:postgresql://${pgUrl}/postgres", user = pgUser, password = pgPwd)
    }


  //val dbTestSqlite = Database.forURL("jdbc:sqlite:test.db", driver = "org.sqlite.JDBC")

  def schemas: driver.DDL

  def createSchema(schema: String) = sqlu"""CREATE SCHEMA IF NOT EXISTS "#$schema";"""


  def createTables = schemas.createIfNotExists

  def dropTables = schemas.drop

  def setupActions = Seq(schemaName.map(createSchema), Some(createTables)).filter(_.isDefined).map(_.get)

  def setup = DBIO.seq(setupActions: _*).transactionally

  def create[T, PK](pkType: Class[PK], element: T)(implicit dao: Dao[T, PK]): dao.thisDriver.api.DBIOAction[T, dao.thisDriver.api.NoStream, Write] = dao.create(element)

  def update[T, PK](pkType: Class[PK], element: T)(implicit dao: Dao[T, PK]): dao.thisDriver.api.DBIOAction[T, dao.thisDriver.api.NoStream, Write] = dao.update(element)

  def read[T, PK](dtoType: Class[T], id: PK)(implicit dao: Dao[T, PK]): FixedSqlStreamingAction[Seq[T], T, Read] = dao.read(id)

  def delete[T, PK](dtoType: Class[T], id: PK)(implicit dao: Dao[T, PK]): FixedSqlAction[Int, dao.thisDriver.api.NoStream, Write] = dao.delete(id)

  def readAll[T, PK](dtoType: Class[T], pkType: Class[PK])(implicit dao: Dao[T, PK]): FixedSqlStreamingAction[Seq[T], T, Read] = dao.readAll

}
