package be.cetic.inah.commons.database.sql

import slick.dbio.Effect.{Read, Write}
import slick.jdbc.JdbcProfile
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}


trait DaoTableQuery[T]

trait Dao[T, IdType] {
  val thisDriver: JdbcProfile

  import thisDriver.api._

  def create(element: T): DBIOAction[T, NoStream, Write]

  def update(element: T): DBIOAction[T, NoStream, Write]

  def read(id: IdType): FixedSqlStreamingAction[Seq[T], T, Read]

  def readAll: FixedSqlStreamingAction[Seq[T], T, Read]

  def delete(id: IdType): FixedSqlAction[Int, NoStream, Write]

}

