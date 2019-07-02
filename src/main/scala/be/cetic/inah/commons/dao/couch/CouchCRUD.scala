package be.cetic.inah.commons.dao.couch


import org.lightcouch.Document
import spray.json.JsValue

trait CouchCRUD

trait CRUDResponse

trait CRUDFailure extends CRUDResponse{
  val key: String
  val reason: Throwable
}

trait CRUDSuccess extends CRUDResponse{
  val key: String
  val rev: String
}

case class Create(key: String, content: JsValue) extends CouchCRUD

trait CreateResponse extends CRUDResponse

case class CreateSuccess(key: String, rev: String, content: JsValue) extends CreateResponse with CRUDSuccess

case class CreateFailure(key: String, reason: Throwable) extends CreateResponse with CRUDFailure

case class Read(key: String) extends CouchCRUD


trait ReadAllResponse

case class ReadAllSuccess(responses: Seq[ReadResponse]) extends ReadAllResponse

case class ReadAllFailure(reason: Throwable) extends ReadAllResponse

case object ReadAll

trait ReadResponse extends CRUDResponse

case class ReadSuccess(key: String, rev: String, content: JsValue) extends ReadResponse with CRUDSuccess

case class ReadFailure(key: String, reason: Throwable) extends ReadResponse with CRUDFailure


case class Update(key: String, content: JsValue) extends CouchCRUD

trait UpdateResponse extends CRUDResponse

case class UpdateSuccess(key: String, rev: String, content: JsValue) extends UpdateResponse with CRUDSuccess

case class UpdateFailure(key: String, reason: Throwable) extends UpdateResponse with CRUDFailure


case class Delete(key: String) extends CouchCRUD

trait DeleteResponse extends CRUDResponse

case class DeleteSuccess(key: String, rev: String) extends DeleteResponse with CRUDSuccess

case class DeleteFailure(key: String, reason: Throwable) extends DeleteResponse with CRUDFailure
