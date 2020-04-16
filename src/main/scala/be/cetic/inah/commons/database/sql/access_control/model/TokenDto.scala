package be.cetic.inah.commons.database.sql.access_control.model

import be.cetic.inah.commons.database.sql.access_control.AccessControl
import be.cetic.inah.commons.database.sql.{DriverComponent, DtoWithId}
import slick.dbio.Effect.{Read, Write}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor


case class TokenDto(id: Option[Int], token: String, status: String, createdAt: Long, expiresAt: Long) extends DtoWithId[Int]

trait TokensDtoMultiDb extends DriverComponent with AccessControl {

  import driver.api._

  class TokensDto(tag: Tag) extends Table[TokenDto](tag, schemaName.orElse(accessControlSchemaName), "tokens") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def token = column[String]("token", O.Unique)

    def status = column[String]("status")

    def createdAt = column[Long]("created_at")

    def expiresAt = column[Long]("expires_at")

    def * = (id.?, token, status, createdAt, expiresAt) <> (TokenDto.tupled, TokenDto.unapply)
  }

  implicit val dispatcher: ExecutionContextExecutor

  object TokensDao {

    val tokens = TableQuery[TokensDto]

    private val aAutoInc = tokens returning tokens.map(_.id)

    def create(element: TokenDto): DBIOAction[TokenDto, NoStream, Write] = {
      (aAutoInc += element).map(id => element.copy(id = Some(id)))
    }

    def update(element: TokenDto): DBIOAction[Any, NoStream, Write] = aAutoInc.insertOrUpdate(element)
      .map { maybeId =>
        maybeId.map { id =>
          element.copy(id = Some(id))
        }
          .getOrElse(element)
      }

    private def queryId(id: Int) = tokens.filter(_.id === id)

    def read(id: Int): FixedSqlStreamingAction[Seq[TokenDto], TokenDto, Read] = queryId(id).result

    def read(token: String): FixedSqlStreamingAction[Seq[TokenDto], TokenDto, Read] = tokens.filter(_.token === token).result


    def readAll: FixedSqlStreamingAction[Seq[TokenDto], TokenDto, Read] = tokens.result

    def delete(id: Int): FixedSqlAction[Int, NoStream, Write] = queryId(id).delete

    def delete(token: String): FixedSqlAction[Int, NoStream, Write] = tokens.filter(_.token === token).delete
  }

}