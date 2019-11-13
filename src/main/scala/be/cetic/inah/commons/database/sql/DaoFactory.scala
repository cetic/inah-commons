package be.cetic.inah.commons.database.sql


import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContextExecutor

trait DaoFactory {
  val driver: JdbcProfile
  val dbProfile: String = ""
  val schemaName: Option[String] = None
  implicit val dispatcher: ExecutionContextExecutor

  import driver.api._

  val db = Database.forConfig(dbProfile)

  def createSchema(schema: String) = sqlu"""CREATE SCHEMA IF NOT EXISTS "#$schema";"""

  def schemas: driver.DDL

  def createTables = schemas.create

  def dropTables = schemas.drop

  val setupActions = Seq(schemaName.map(createSchema), Some(createTables)).filter(_.isDefined).map(_.get)

  def setup = DBIO.seq(setupActions: _*)


}
