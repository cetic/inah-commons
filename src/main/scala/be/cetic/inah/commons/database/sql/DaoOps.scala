package be.cetic.inah.commons.database.sql

import be.cetic.inah.commons.database.sql.management.model.ResourceDto
import slick.dbio.Effect.{Read, Write}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

trait DaoOps {
  def create[T, PK](pkType: Class[PK], element: T)(implicit dao: Dao[T, PK]): dao.thisDriver.api.DBIOAction[T, dao.thisDriver.api.NoStream, Write] = dao.create(element)

  def update[T, PK](pkType: Class[PK], element: T)(implicit dao: Dao[T, PK]): dao.thisDriver.api.DBIOAction[T, dao.thisDriver.api.NoStream, Write] = dao.update(element)

  def read[T, PK](dtoType: Class[T], id: PK)(implicit dao: Dao[T, PK]): FixedSqlStreamingAction[Seq[T], T, Read] = dao.read(id)

  def delete[T, PK](dtoType: Class[T], id: PK)(implicit dao: Dao[T, PK]): FixedSqlAction[Int, dao.thisDriver.api.NoStream, Write] = dao.delete(id)

  def readAll[T, PK](dtoType: Class[T], pkType: Class[PK])(implicit dao: Dao[T, PK]): FixedSqlStreamingAction[Seq[T], T, Read] = dao.readAll

}
