package be.cetic.inah.commons.authentication

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait UserJsonProtocol extends DefaultJsonProtocol {
  implicit val userJsonFormat : RootJsonFormat[User] = jsonFormat6(User)

}
