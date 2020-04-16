package be.cetic.inah.commons.database.sql.management.serialization

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import be.cetic.inah.commons.database.sql.management.model.ProjectDto
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait ProjectDtoJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {

  implicit val projectDtoJsonFormat: RootJsonFormat[ProjectDto] = jsonFormat5(ProjectDto)

}
