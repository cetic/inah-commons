package be.cetic.inah.commons.database.sql.management

import be.cetic.inah.commons.database.sql.SchemaName

trait Management extends SchemaName {
  val managementSchemaName: Option[String] = Some("management")
}
