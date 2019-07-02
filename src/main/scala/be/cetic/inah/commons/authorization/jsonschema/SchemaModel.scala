package be.cetic.inah.commons.authorization.jsonschema

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, DeserializationException, JsArray, JsBoolean, JsObject, JsString, JsValue, JsonFormat, RootJsonFormat}

trait SchemaModel

trait RootSchemaModel extends SchemaModel

case object ModelLeaf extends SchemaModel

case class ModelKeyValue(key: String, value: SchemaModel, required: Boolean = false) extends RootSchemaModel

case class ModelObject(fields: Set[ModelKeyValue]) extends RootSchemaModel

case class SchemaRef(name: String) extends RootSchemaModel


object ModelObject {
  private def normalizeArg[A, B](v: A): ModelKeyValue = v match {
    case k: ModelKeyValue => k
    case s: String => ModelKeyValue(s, ModelLeaf)
    case t: (String, B) =>
      t._2 match {
        case b: Boolean => ModelKeyValue(t._1, ModelLeaf, b)
        case m: SchemaModel => ModelKeyValue(t._1, m)
        case _ => ???
      }
    case stb: (String, SchemaModel, Boolean) => ModelKeyValue(stb._1, stb._2, stb._3)
    case _ => ???
  }

  def apply[A](models: A*): ModelObject = {
    if (models.isEmpty) return ModelObject(Nil)
    ModelObject(models.map(normalizeArg).toSet)
  }

  def apply(map: Map[String, SchemaModel]): ModelObject = {
    val fields = map.map { case (k, v) => ModelKeyValue(k, v) }.toSet
    new ModelObject(fields)
  }

}

case class ModelArray(baseObject: SchemaModel) extends RootSchemaModel


case class OneOf(objects: SchemaModel*) extends RootSchemaModel

object ModelArray {
  def apply(): ModelArray = ModelArray(ModelLeaf)
}


trait SchemaModelJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {

  implicit object SchemaModelFormat extends RootJsonFormat[SchemaModel] {
    override def write(obj: SchemaModel): JsValue = obj match {
      case ModelLeaf => JsString("leaf")
      case ModelKeyValue(key, value, req) => JsObject(Map("key" -> JsString(key), "value" -> write(value), "required" -> JsBoolean(req)))
      case o: ModelObject => JsObject(Map("fields" -> JsArray(o.fields.map(write).toVector)))
      case ModelArray(baseObject) => JsObject(Map("baseObject" -> write(baseObject)))
      case oneOf: OneOf => JsObject("oneOf" -> JsArray(oneOf.objects.map(write).toVector))
      case SchemaRef(name) => JsObject(Map("ref" -> JsString(name)))
      case o => throw DeserializationException(s"Cannot write object $o")
    }


    override def read(json: JsValue): SchemaModel = {
      try {
        json.asInstanceOf[JsString].value match {
          case "leaf" => ModelLeaf
          case _ => ???
        }
      } catch {
        case e: Exception =>
          val fields = json.asJsObject().fields
          val keys = fields.keySet
          val isKeyValue = keys.contains("key")
          val isObject = keys.contains("fields")
          val isArray = keys.contains("baseObject")
          val isRef = keys.contains("ref")
          val isOneOf = keys.contains("oneOf")

          json match {
            case _ if isKeyValue => ModelKeyValue(
              fields("key").asInstanceOf[JsString].value,
              read(fields("value")),
              fields("required").asInstanceOf[JsBoolean].value
            )
            case _ if isArray => ModelArray(
              read(fields("baseObject"))
            )
            case _ if isObject =>
              val atts: Set[ModelKeyValue] = fields("fields").asInstanceOf[JsArray].elements.map(read).map(_.asInstanceOf[ModelKeyValue]).toSet
              ModelObject(atts)

            case _ if isRef => SchemaRef(fields("ref").asInstanceOf[JsString].value)

            case _ if isOneOf => OneOf(fields("oneOf").asInstanceOf[JsArray].elements.map(el => read(el)): _*)

            case o => throw DeserializationException(s"Cannot read object $o")
          }
      }
    }
  }

}