package be.cetic.inah.commons.database.sql.access_control

import be.cetic.inah.commons.database.sql.SchemaName

trait AccessControl extends SchemaName{
 val accessControlSchemaName : Option[String] = Some("access-control")
}
