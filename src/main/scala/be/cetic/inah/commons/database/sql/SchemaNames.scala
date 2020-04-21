package be.cetic.inah.commons.database.sql

object SchemaNames {

  var accessControlSchemaName : Option[String] = Some("access_control")
  var managementSchemaName: Option[String] = Some("management")
  var populationSchemaName: Option[String] = Some("inah")

  def setNoGivenSchema = {
    accessControlSchemaName = None
    managementSchemaName = None
    populationSchemaName = None
  }

  def schemaNames = accessControlSchemaName.toSeq ++ managementSchemaName.toSeq ++ populationSchemaName.toSeq

}
