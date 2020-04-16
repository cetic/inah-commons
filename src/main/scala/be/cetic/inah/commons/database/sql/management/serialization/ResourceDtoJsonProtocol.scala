package be.cetic.inah.commons.database.sql.management.serialization

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import be.cetic.inah.commons.database.sql.management.model.ResourceDto
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait ResourceDtoJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val resourceDtoJsonFormat: RootJsonFormat[ResourceDto] = jsonFormat6(ResourceDto)

}
