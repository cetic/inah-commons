package be.cetic.inah.commons.database.sql.management.serialization

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import be.cetic.inah.commons.database.sql.management.model.UserDto
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait UserDtoJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {

  implicit val userDtoJsonFormat : RootJsonFormat[UserDto] = jsonFormat4(UserDto)


}
