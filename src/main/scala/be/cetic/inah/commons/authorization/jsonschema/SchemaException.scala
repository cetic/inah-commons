package be.cetic.inah.commons.authorization.jsonschema

trait SchemaModelException extends Exception

class ModelException(message: String) extends SchemaModelException

object ModelException extends SchemaModelException {
  def apply(msg: String): ModelException = new ModelException(msg)
}


class ModelRequirementException(message: String) extends SchemaModelException

object ModelRequirementException extends SchemaModelException {
  def apply(msg: String): ModelException = new ModelException(msg)
}
