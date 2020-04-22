package be.cetic.inah.commons.database.sql.management.model

import java.sql.Blob

import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import be.cetic.inah.commons.database.sql.management.ManagementResource
import slick.dbio.Effect.Write
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor


case class UserDetailsDto (email : String, firstName : String, lastName : String, position : String, cv :Option[Blob], adress : String,
                           organisation : Option [String], userId : String ) extends ManagementResource
trait UsersDetailsMultiDb extends DriverComponent with UsersDtoMultiDb {


  import driver.api._

  class UsersDetailsDto (tag : Tag ) extends Table[UserDetailsDto] (tag, SchemaNames.managementSchemaName, "user_details") {

    def email = column [String] ("email", O.PrimaryKey)
    def firstName = column[String]("first_name")
    def lastName = column[String]("last_name")
    def position = column[String]("position")
    def cv = column[Option[Blob]]("cv")
    def adress = column[String]("adress")
    def organisation = column[Option[String]]("organisation")
    def userId = column[String]("user_id")
    def user = foreignKey("user_details_user_fk", userId, UserDao.users)(_.email)
    def * = (email, firstName,lastName, position, cv, adress, organisation, userId ) <> (UserDetailsDto.tupled, UserDetailsDto.unapply)

  }


  implicit val dispatcher: ExecutionContextExecutor

  implicit object UsersDetailsDao extends Dao[UserDetailsDto, String] {


    val thisDriver = driver
    val userDetails = TableQuery[UsersDetailsDto]

    def create(element: UserDetailsDto): DBIOAction[UserDetailsDto, NoStream, Write] = {

      (userDetails+=element).map(_=>element)
    }

    def update(element: UserDetailsDto): DBIOAction[UserDetailsDto, NoStream, Write] = {
      userDetails.insertOrUpdate(element).map(_=>element)
    }

    def queryByMail (email : String) = userDetails.filter(u => u.email === email)
    def read(id: String): FixedSqlStreamingAction[Seq[UserDetailsDto], UserDetailsDto, Effect.Read] = queryByMail(id).result

    def readAll = userDetails.result

    def delete(id: String) = queryByMail(id).delete
  }

}