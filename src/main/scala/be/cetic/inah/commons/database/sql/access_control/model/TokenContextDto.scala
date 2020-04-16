package be.cetic.inah.commons.database.sql.access_control.model

import be.cetic.inah.commons.database.sql.access_control.AccessControl
import be.cetic.inah.commons.database.sql.{DriverComponent, DtoCompositeId}
import slick.dbio.Effect.{Read, Write}
import slick.jdbc.GetResult
import slick.lifted.ForeignKeyQuery
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor


case class TokenContextDto(tokenId: Int, contextId: Int) extends DtoCompositeId

trait TokenContextsDtoMultiDb extends DriverComponent with TokensDtoMultiDb with ContextsDtoMultiDb with AccessControl {

  import driver.api._


  implicit val resultToAccessTokenContextsMapper: GetResult[TokenContextDto] = GetResult(r=> TokenContextDto(r.<<, r.<<))

  class AccessTokenContextsDto(tag: Tag) extends Table[TokenContextDto](tag,  schemaName.orElse(accessControlSchemaName), "token_contexts") {
    def tokenId = column[Int]("token_id")

    def contextId = column[Int]("context_id")

    def pk = primaryKey("token_context_pk", (tokenId, contextId))

    def * = (tokenId, contextId) <> (TokenContextDto.tupled, TokenContextDto.unapply)

    def token: ForeignKeyQuery[TokensDto, TokenDto] = foreignKey("token_context_token_fk", tokenId, TokensDao.tokens)(_.id)

    def context: ForeignKeyQuery[ContextsDto, DataDto] = foreignKey("token_context_context_fk", contextId, ContextsDao.contexts)(_.id)
  }


  implicit val dispatcher: ExecutionContextExecutor

  object TokenContextsDao {
    val tokenContexts = TableQuery[AccessTokenContextsDto]

    def create(element: TokenContextDto): DBIOAction[TokenContextDto, NoStream, Write] = {
      (tokenContexts += element).map(_ => element)
    }

    def update(element: TokenContextDto): DBIOAction[Any, NoStream, Write] = tokenContexts.insertOrUpdate(element).map { _ => element}


    private def queryId(tokenId: Int, contextId: Int) = tokenContexts
      .filter(e=> e.tokenId === tokenId && e.contextId === contextId)

    private def queryId(tokenId: Int) = tokenContexts
      .filter(e=> e.tokenId ===  tokenId)

    def read(tokenId: Int): FixedSqlStreamingAction[Seq[TokenContextDto], TokenContextDto, Read] = queryId(tokenId).result
    def read(tokenId: Int, contextId: Int): FixedSqlStreamingAction[Seq[TokenContextDto], TokenContextDto, Read] = queryId(tokenId, contextId).result

    def readAll: FixedSqlStreamingAction[Seq[TokenContextDto], TokenContextDto, Read] = tokenContexts.result

    def delete(tokenId: Int): FixedSqlAction[Int, NoStream, Write] = queryId(tokenId).delete
    def delete(tokenId: Int, contextId: Int): FixedSqlAction[Int, NoStream, Write] = queryId(tokenId, contextId).delete
  }

}
