package be.cetic.inah.commons.database.sql.management.serialization

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import be.cetic.inah.commons.database.sql.management.model.ProjectResourceDto
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait ProjectResourcesJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val projectResourceJsonFormat: RootJsonFormat[ProjectResourceDto] = jsonFormat2(ProjectResourceDto)
}
