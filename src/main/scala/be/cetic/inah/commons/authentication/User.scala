package be.cetic.inah.commons.authentication

case class  User(id: Option[String], username: String, password: String, role: String, projects: Seq[String], datasources: Seq[Int] = Nil) {
  def view() = this.copy(id = None, password = "")
}
