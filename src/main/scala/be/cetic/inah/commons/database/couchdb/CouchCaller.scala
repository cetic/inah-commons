package be.cetic.inah.commons.database.couchdb

import com.google.gson.{Gson, JsonObject}
import org.lightcouch.CouchDbClient
import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue, _}

import scala.collection.JavaConverters._

class CouchCallerException(msg: String) extends Exception

object CouchCallerException {
  def apply(error: String, reason: String): CouchCallerException = new CouchCallerException(s"Error: $error, because of $reason")
}

trait CouchCaller extends DefaultJsonProtocol {
  val client: CouchDbClient

  private def wrapToGson(key: String, content: JsValue, rev: Option[String] = None): java.util.Map[String, Any] = {
    var obj = Map("_id" -> JsString(key), "content" -> content)
    rev.foreach { r => obj = obj.updated("_rev", JsString(r)) }
    val couchContent = JsObject(obj).toString
    new Gson().fromJson(couchContent, classOf[java.util.Map[String, Any]])
  }

  def create(c: Create): CreateResponse = {
    try {
      val resp = client.save(wrapToGson(c.key, c.content))
      CreateSuccess(resp.getId, resp.getRev, c.content)
    } catch {
      case t: Throwable =>
        CreateFailure(c.key, t)
    }
  }

  def read(r: Read): ReadResponse = {
    try {
      val resp = client.find(classOf[JsonObject], r.key)
      val result = resp.toString.parseJson
      val rev = result.asJsObject().fields("_rev").asInstanceOf[JsString].value
      val content = result.asJsObject.fields("content")
      ReadSuccess(r.key, rev, content)
    } catch {
      case t: Throwable =>
        ReadFailure(r.key, t)
    }
  }

  def readAll(): ReadAllResponse = {
    try {
      val alldocs = client.findAny(classOf[JsonObject], client.getDBUri + "_all_docs")
      val results = alldocs.get("rows").getAsJsonArray
      ReadAllSuccess(results.iterator().asScala.toVector.map { json =>
        val key = json.getAsJsonObject.get("id").getAsString()
        read(Read(key))
      })
    } catch {
      case t: Throwable =>
        ReadAllFailure(t)
    }
  }

  def update(u: Update): UpdateResponse = {
    val previous = read(Read(u.key))
    previous match {
      case ReadSuccess(key, rev, _) =>
        try {
          val resp = client.update(wrapToGson(key, u.content, Some(rev)))
          UpdateSuccess(resp.getId, resp.getRev, u.content)
        } catch {
          case t: Throwable =>
            UpdateFailure(key, t)
        }
      case ReadFailure(k, t) => UpdateFailure(k, t)
    }
  }

  def delete(d: Delete): DeleteResponse = {
    val previous = read(Read(d.key))
    previous match {
      case ReadSuccess(key, rev, _) =>
        try {
          val resp = client.remove(key, rev)
          DeleteSuccess(resp.getId, resp.getRev)
        } catch {
          case t: Throwable =>
            DeleteFailure(key, t)
        }
      case ReadFailure(k, t) => DeleteFailure(k, t)
    }
  }

  def crud(m: CouchCRUD): CRUDResponse = {
    m match {
      case c: Create => create(c)
      case r: Read => read(r)
      case u: Update => update(u)
      case d: Delete => delete(d)
      case _ => throw new Exception("Unsupported CRUD operation")
    }
  }

}
