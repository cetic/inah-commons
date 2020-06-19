package be.cetic.inah.commons.database.sql.temp_objects

import be.cetic.inah.commons.database.sql.management.SqlManagementJsonProtocol
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait SqlTempObjectsJsonProtocol extends DefaultJsonProtocol with SqlManagementJsonProtocol{
  implicit val datasetSyncDtoJsonFormat : RootJsonFormat[DatasetSyncDto] = jsonFormat4(DatasetSyncDto)
}
