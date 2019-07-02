package be.cetic.inah.commons.authorization.jsonschema

import spray.json._

case class ModelRef(name: String, value: RootSchemaModel)

case class SchemaDescription(model: RootSchemaModel, refMap: Seq[ModelRef]=Nil) {
  def toDescriptor: SchemaDescriptor = new SchemaDescriptor(model, refMap.map(el=>el.name->el.value).toMap)
}

trait SchemaDescriptionJsonProtocol extends SchemaModelJsonProtocol {

  implicit object SchemaDescriptionJsonFormat extends JsonFormat[SchemaDescription] {
    override def write(obj: SchemaDescription): JsValue = {
      val modelJson = obj.model.asInstanceOf[SchemaModel].toJson
      val refMapJson = JsArray(obj.refMap.map { case r =>

        val name = JsString(r.name)
        val model = r.value.asInstanceOf[SchemaModel].toJson

        JsObject(Map("name" -> name, "model" -> model))
      }.toVector
      )

      JsObject("model" -> modelJson, "refMap" -> refMapJson)
    }

    override def read(json: JsValue): SchemaDescription = {
      val fields = json.asJsObject().fields
      val model = fields("model").convertTo[SchemaModel].asInstanceOf[RootSchemaModel]
      val refMap = fields("refMap").asInstanceOf[JsArray].elements.map { ref =>
        val name = ref.asJsObject.fields("name").asInstanceOf[JsString].value
        val refModel = ref.asJsObject.fields("model").convertTo[SchemaModel].asInstanceOf[RootSchemaModel]
        ModelRef(name ,refModel)
      }
      SchemaDescription(model, refMap)
    }
  }

}

class SchemaDescriptor(modelObject: RootSchemaModel, schemaRefMap: Map[String, RootSchemaModel] = Map()) {

  def complies[A](json: JsonLike[A]): Either[Boolean, Throwable] = {
    try {
      Left(checkModel(modelObject, json))
    } catch {
      case c: ClassCastException => Right(c)
      case e: SchemaModelException => Right(e)
      case t: Throwable => throw t
    }
  }


  private def checkModelArray[A](model: ModelArray, jsonArray: JsonLike[A]): Boolean = {
    model.baseObject match {
      case ModelLeaf =>
        jsonArray.toSeq
        true
      case r: RootSchemaModel =>
        jsonArray.toSeq.forall { el: A =>
          checkModel(r, new JsonLike(el))
        }
    }
  }

  private def checkOneOf[A](model: OneOf, jsonObj: JsonLike[A]): Boolean = {
   val isOneOf =  model.objects.exists {
      case option: RootSchemaModel =>
        try {
          checkModel(option, jsonObj)
        }
        catch {
          case e: Exception => false
        }
      case ModelLeaf => true
    }

    if (isOneOf) true else throw new ModelException("Model does not comply with any options")
  }

  private def checkModel[A](model: RootSchemaModel, jsonObj: JsonLike[A]): Boolean = {
    model match {
      case r@SchemaRef(refName) => return checkModel(schemaRefMap(refName), jsonObj)
      case a: ModelArray => return checkModelArray(a, jsonObj)
      case o: OneOf => return checkOneOf(o, jsonObj)
      case _ =>
    }

    val keys = jsonObj.keys

    model match {
      case ModelObject(fields) if !keys.subsetOf(fields.map(_.key)) =>
        val problems = keys.diff(fields.map(_.key))
        throw new ModelException(s"Fields ${problems.mkString(", ")} are not allowed.")

      case ModelObject(fields) if !fields.filter(_.required).map(_.key).subsetOf(keys) =>
        val problems = fields.filter(_.required).map(_.key).diff(keys)
        throw new ModelRequirementException(s"Fields ${problems.mkString(", ")} are required.")

      case ModelObject(fields) =>
        fields.forall {
          case ModelKeyValue(key, value, required) if value == ModelLeaf => true
          case ModelKeyValue(key, value, required) if keys.contains(key) =>
            val sub = jsonObj.extract(key)
            checkModel(value.asInstanceOf[RootSchemaModel], new JsonLike(sub))
          case _ => true
        }

    }

  }
}
