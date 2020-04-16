package be.cetic.inah.commons.database.sql

trait Dto

trait DtoWithId[PK] extends Dto {
  val id: Option[PK]
}

trait DtoWithProvidedId[PK] extends Dto {
  val id: PK
}

trait DtoCompositeId extends Dto


case class RelationOps[FK, PK](read: PK => RelationRead[PK], create: (PK, FK) => RelationCreate[PK, FK],
                               delete: (PK, FK) => RelationDelete[PK, FK])

case class CRUDOps[OType <: Dto, PK](create: OType => CrudCreate[OType], read: PK => CrudRead[PK], update: OType => CrudUpdate[OType],
                                     delete: PK => CrudDelete[PK], readAll: () => CrudReadAll)


trait CRUD

object CRUD {
  type CRUDOp[PK] = PK => CRUD
  val s = Seq(32)
}

trait CrudCreate[T <: Dto] extends CRUD {
  val obj: T
}

trait CrudRead[PK] extends CRUD {
  val id: PK
}

trait CrudUpdate[T <: Dto] extends CRUD {
  val obj: T
}

trait CrudDelete[PK] extends CRUD {
  val id: PK
}

trait CrudReadAll

trait RelationCRD

trait RelationRead[FK1] extends RelationCRD {
  val id: FK1
}

trait RelationCreate[FK1, FK2] extends RelationCRD {
  val id1: FK1
  val id2: FK2
}

trait RelationDelete[FK1, FK2] extends RelationCRD {
  val id1: FK1
  val id2: FK2
}