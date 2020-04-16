package be.cetic.inah.commons.database.sql.access_control.model

import be.cetic.inah.commons.database.sql.access_control.AccessControl
import be.cetic.inah.commons.database.sql.{DriverComponent, DtoCompositeId}
import slick.dbio.Effect.{Read, Write}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class TokenRightDto(tokenId: Int, rightId: Int) extends DtoCompositeId

trait TokenAccessesDtoMultiDb extends DriverComponent with TokensDtoMultiDb with RightsDtoMultiDb with AccessControl {

  import driver.api._

  class TokenRightsDto(tag: Tag) extends Table[TokenRightDto](tag,  schemaName.orElse(accessControlSchemaName), "token_rights") {
    def tokenId = column[Int]("token_id")

    def rightId = column[Int]("right_id")

    def pk = primaryKey("token_access_pk", (tokenId, rightId))

    def * = (tokenId, rightId) <> (TokenRightDto.tupled, TokenRightDto.unapply)

    def token = foreignKey("access_token_access_token_fk", tokenId, TokensDao.tokens)(_.id)

    def right = foreignKey("access_token_access_access_fk", rightId, RightsDao.rights)(_.id)
  }


  implicit val dispatcher: ExecutionContextExecutor

  object TokenRightsDao {

    val tokenRights = TableQuery[TokenRightsDto]

    def create(element: TokenRightDto): DBIOAction[TokenRightDto, NoStream, Write] = {
      (tokenRights += element).map(_ => element)
    }

    def update(element: TokenRightDto): DBIOAction[Any, NoStream, Write] = tokenRights.insertOrUpdate(element).map { _ => element}


    private def queryId(tokenId: Int, accessId: Int) = tokenRights
      .filter(e=> e.tokenId === tokenId && e.rightId === accessId)

    private def queryId(tokenId: Int) = tokenRights
      .filter(e=> e.tokenId ===  tokenId)

    def read(tokenId: Int): FixedSqlStreamingAction[Seq[TokenRightDto], TokenRightDto, Read] = queryId(tokenId).result
    def read(tokenId: Int, accessId: Int): FixedSqlStreamingAction[Seq[TokenRightDto], TokenRightDto, Read] = queryId(tokenId, accessId).result

    def readAll: FixedSqlStreamingAction[Seq[TokenRightDto], TokenRightDto, Read] = tokenRights.result

    def delete(tokenId: Int): FixedSqlAction[Int, NoStream, Write] = queryId(tokenId).delete
    def delete(tokenId: Int, accessId: Int): FixedSqlAction[Int, NoStream, Write] = queryId(tokenId, accessId).delete
  }

}