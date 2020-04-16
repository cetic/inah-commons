package be.cetic.inah.commons.database.sql.management.serialization

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import be.cetic.inah.commons.database.sql.management.model.ProjectDatasourceDto
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait ProjectDatasourcesJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val projectDatasourceJsonFormat: RootJsonFormat[ProjectDatasourceDto] = jsonFormat3(ProjectDatasourceDto)

}
