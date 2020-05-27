package be.cetic.inah.commons.database.sql.management.model

import be.cetic.inah.commons.authentication.PasswordUtil
import be.cetic.inah.commons.database.sql.enumeration.UserRoles
import be.cetic.inah.commons.database.sql.management.ManagementResource
import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import slick.dbio.Effect.{Read, Write}
import slick.sql.FixedSqlStreamingAction

import scala.concurrent.ExecutionContextExecutor
import scala.util.Random

case class  UserDto(email: String, name: String, password: String, role: String = UserRoles.USER) extends ManagementResource {
  def view() = this.copy(password = "")
}


trait UsersDtoMultiDb extends DriverComponent with PasswordUtil {

  import driver.api._

  class UsersDto(tag: Tag) extends Table[UserDto](tag, SchemaNames.managementSchemaName, "user") {

    def email = column[String]("email", O.PrimaryKey)

    def name = column[String]("name")

    def password = column[String]("password")

    def role = column[String]("role")

    def * = (email, name, password, role) <> (UserDto.tupled, UserDto.unapply)
  }

  implicit val dispatcher: ExecutionContextExecutor

  implicit object UserDao extends Dao[UserDto, String] {

    val thisDriver = driver
    val users = TableQuery[UsersDto]

    def create(element: UserDto): DBIOAction[UserDto, NoStream, Write] = {
      val pwd = Random.alphanumeric.take(15).toSeq.mkString("")
      val hashedPwd = hashPassword(pwd)
      val newUser = element.copy(password = hashedPwd)
      val toReturn = element.copy(password = pwd)
      (users += newUser).map(_ => toReturn)
    }

    def update(element: UserDto): DBIOAction[UserDto, NoStream, Write] = {

      users.insertOrUpdate(element).map(_ => element)


    }

    def queryByMail(email: String) = users.filter(u => u.email === email)

    def delete(id: String) = queryByMail(id).delete

    def read(id: String): FixedSqlStreamingAction[Seq[UserDto], UserDto, Read] = queryByMail(id).result


    def readAll = users.result


  }


}
