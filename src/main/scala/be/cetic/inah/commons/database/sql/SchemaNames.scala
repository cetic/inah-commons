package be.cetic.inah.commons.database.sql

object SchemaNames {

  var accessControlSchemaName: Option[String] = Some("access_control")
  var managementSchemaName: Option[String] = Some("management")
  var populationSchemaName: Option[String] = Some("inah")
  var tempObjectsSchemaName: Option[String] = Some("temp_objects")

  def setNoGivenSchema = {
    accessControlSchemaName = None
    managementSchemaName = None
    populationSchemaName = None
    tempObjectsSchemaName = None
  }


  def schemaNames = (accessControlSchemaName ++ managementSchemaName ++ populationSchemaName ++ tempObjectsSchemaName).toSeq

}
