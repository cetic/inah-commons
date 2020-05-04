package be.cetic.inah.commons.authentication

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class  User(id: Option[String], username: String, password: String, role: String, projects: Seq[String], datasources: Seq[Int] = Nil) {
  def view() = this.copy(id = None, password = "")
}

trait UserJsonProtocol extends DefaultJsonProtocol {
  implicit val userJsonFormat: RootJsonFormat[User] = jsonFormat6(User)
}
