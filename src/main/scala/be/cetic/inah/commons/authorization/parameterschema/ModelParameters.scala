package be.cetic.inah.commons.authorization.parameterschema

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import be.cetic.inah.commons.authorization.jsonschema._
import spray.json.{DefaultJsonProtocol, JsValue, RootJsonFormat, _}

trait Parameter {
  def toSchemaModel: SchemaModel
}

case class NamedParameter(name: String, required: Boolean = true) extends Parameter {
  def toSchemaModel = ModelKeyValue(name, ModelLeaf, required)
}

case object AnyParameter extends Parameter {
  def toSchemaModel: ModelLeaf.type = ModelLeaf
}

case class ModelParameters(parameters: Parameter*) {
  def toModelObject = ModelObject(parameters.map(_.toSchemaModel): _*)

  def toDescriptor: SchemaDescriptor = SchemaDescription(toModelObject).toDescriptor
}


trait ModelParameterJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val namedParameterJsonFormat: RootJsonFormat[NamedParameter] = jsonFormat2(NamedParameter)

  implicit object ParameterJsonFormat extends JsonFormat[Parameter] {
    override def write(obj: Parameter): JsValue = {
      obj match {
        case n: NamedParameter => n.toJson
        case AnyParameter => JsString("*")
        case _ => ???
      }
    }

    override def read(json: JsValue): Parameter = {
      json match {
        case JsString("*") => AnyParameter
        case _ => json.convertTo[NamedParameter]
      }
    }
  }

  implicit object ModelParameterJsonFormat extends RootJsonFormat[ModelParameters] {
    override def write(obj: ModelParameters): JsValue = {
      JsObject(("parameters", JsArray(obj.parameters.map(_.toJson).toVector)))
    }

    override def read(json: JsValue): ModelParameters = {
      ModelParameters(json.asJsObject.fields("parameters").asInstanceOf[JsArray].elements.map(_.convertTo[NamedParameter]): _*)
    }
  }

}