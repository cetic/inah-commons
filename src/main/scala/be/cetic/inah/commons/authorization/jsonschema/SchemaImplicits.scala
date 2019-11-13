package be.cetic.inah.commons.authorization.jsonschema

import com.google.gson.JsonElement
import org.codehaus.groovy.runtime.IteratorClosureAdapter
import spray.json.{JsArray, JsValue}

import scala.collection.{GenTraversableLike, MapLike}
import scala.collection.JavaConverters._

object SchemaImplicits {
  implicit def toSchemaOpCouple[A, B](t: (A, B)): SchemaOpCouple[A, B] = new SchemaOpCouple(t)

  implicit def toSchemaOp(t: String): SchemaOp = new SchemaOp(t)

  implicit def toJsonLike(gson: JsonElement) = new JsonLike[JsonElement](gson)

  implicit def toJsonLike(json: JsValue) = new JsonLike[JsValue](json)

  implicit def toJsonLike(map: Iterable[Any]) = new JsonLike[Iterable[Any]](map)

}

class SchemaOp(t: String) {
  def ! : (String, Boolean) = (t, true)

  def ? : (String, Boolean) = (t, false)
}

class SchemaOpCouple[A, B](t: (A, B)) {
  def ! : (A, B, Boolean) = (t._1, t._2, true)

  def ? : (A, B, Boolean) = (t._1, t._2, false)

}


class JsonLike[A](jsonSource: A) {
  def toSeq[B <: A]: Seq[B] = {
    val seq = jsonSource match {
      case gson: JsonElement => gson.getAsJsonArray.iterator().asScala.toSeq
      case json: JsValue => json.asInstanceOf[JsArray].elements
      case seq: Iterable[Any] =>
        seq.headOption.map {
          case (t: String, a: Any) => throw new ModelException("Expected json array")
          case _ => seq
        }.getOrElse(seq)
      case _ => ???
    }
    seq.asInstanceOf[Seq[B]]
  }

  def keys: Set[String] = {
    jsonSource match {
      case gson: JsonElement => gson.getAsJsonObject.keySet().asScala.toSet
      case map: Iterable[Any] => map.asInstanceOf[Map[String, Any]].keySet
      case json: JsValue => json.asJsObject.fields.keySet
      case e => throw new ModelException(s"${e.getClass} should be iterable. If it is, the key extractor for this class is not implemented yet.")
    }
  }


  def extract(key: String): A = {
    val sub = jsonSource match {
      case gson: JsonElement => gson.getAsJsonObject.get(key)
      case map: GenTraversableLike[Any, Any] => map.asInstanceOf[Map[String, Any]](key).asInstanceOf[Iterable[Any]]
      case json: JsValue => json.asJsObject.fields(key)
      case e => throw new ModelException(s"${e.getClass} should be iterable. If it is, the key extractor for this class is not implemented yet.")
    }

    sub.asInstanceOf[A]
  }
}