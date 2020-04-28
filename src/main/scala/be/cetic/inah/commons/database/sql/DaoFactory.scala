package be.cetic.inah.commons.database.sql

import slick.dbio.Effect.{Read, Write}
import slick.jdbc.JdbcProfile
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

trait DaoFactory extends DaoOps{
  val driver: JdbcProfile
  val dbProfile: Option[String]
  //todo : remove schemaName everywhere
  val schemaName: Option[String] = None

  implicit val dispatcher: ExecutionContextExecutor

  import driver.api._

  val db: driver.backend.DatabaseDef = dbProfile.map(prof => Database.forConfig(prof))
    .getOrElse {
      val pgUrl = sys.env("PG_URL")
      val pgUser = sys.env.getOrElse("PG_USER", "postgres")
      val pgPwd = sys.env.getOrElse("PG_PWD", "postgres")
      Database.forURL(s"jdbc:postgresql://${pgUrl}/postgres", user = pgUser, password = pgPwd)
    }


  //val dbTestSqlite = Database.forURL("jdbc:sqlite:test.db", driver = "org.sqlite.JDBC")

  def schemas: driver.DDL

  def createSchema(schema: String) = sqlu"""CREATE SCHEMA IF NOT EXISTS "#$schema";"""


  def createTables = schemas.create

  def dropTables = schemas.drop

  def setupActions = Seq(schemaName.map(createSchema), Some(createTables)).filter(_.isDefined).map(_.get)

  def setup = DBIO.seq(setupActions: _*).transactionally

}
