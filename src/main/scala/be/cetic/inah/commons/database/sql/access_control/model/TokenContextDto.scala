package be.cetic.inah.commons.database.sql.access_control.model

import be.cetic.inah.commons.database.sql.access_control.AccessControlResource
import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, DtoCompositeId, SchemaNames}
import slick.dbio.Effect.{Read, Write}
import slick.jdbc.GetResult
import slick.lifted.ForeignKeyQuery
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor


case class TokenContextDto(tokenId: Int, contextId: Int) extends DtoCompositeId with AccessControlResource

trait TokenContextsDtoMultiDb extends DriverComponent with TokensDtoMultiDb with ContextsDtoMultiDb {

  import driver.api._

  class AccessTokenContextsDto(tag: Tag) extends Table[TokenContextDto](tag,   SchemaNames.accessControlSchemaName, "token_contexts") {
    def tokenId = column[Int]("token_id")

    def contextId = column[Int]("context_id")

    def pk = primaryKey("token_context_pk", (tokenId, contextId))

    def * = (tokenId, contextId) <> (TokenContextDto.tupled, TokenContextDto.unapply)

    def token: ForeignKeyQuery[TokensDto, TokenDto] = foreignKey("token_context_token_fk", tokenId, TokensDao.tokens)(_.id)

    def context: ForeignKeyQuery[DatasDto, TokenDataDto] = foreignKey("token_context_context_fk", contextId, ContextsDao.contexts)(_.id)
  }


  implicit val dispatcher: ExecutionContextExecutor

  implicit object TokenContextsDao extends Dao[TokenContextDto , (Int, Int)]{
    override val thisDriver = driver

    val tokenContexts = TableQuery[AccessTokenContextsDto]

    def create(element: TokenContextDto): DBIOAction[TokenContextDto, NoStream, Write] = {
      (tokenContexts += element).map(_ => element)
    }

    def update(element: TokenContextDto): DBIOAction[TokenContextDto, NoStream, Write] = tokenContexts.insertOrUpdate(element).map { _ => element}


    private def queryId(tokenId: Int, contextId: Int) = tokenContexts
      .filter(e=> e.tokenId === tokenId && e.contextId === contextId)

    private def queryId(tokenId: Int) = tokenContexts
      .filter(e=> e.tokenId ===  tokenId)

    def read(tokenId: Int): FixedSqlStreamingAction[Seq[TokenContextDto], TokenContextDto, Read] = queryId(tokenId).result
    def read(tokenId: Int, contextId: Int): FixedSqlStreamingAction[Seq[TokenContextDto], TokenContextDto, Read] = queryId(tokenId, contextId).result
    def read(tCId: (Int, Int)): FixedSqlStreamingAction[Seq[TokenContextDto], TokenContextDto, Read] = queryId(tCId._1, tCId._2).result
    def readAll: FixedSqlStreamingAction[Seq[TokenContextDto], TokenContextDto, Read] = tokenContexts.result

    def delete(tokenId: Int): FixedSqlAction[Int, NoStream, Write] = queryId(tokenId).delete
    def delete(tokenId: Int, contextId: Int): FixedSqlAction[Int, NoStream, Write] = queryId(tokenId, contextId).delete
    def delete(tCId: (Int, Int)): FixedSqlAction[Int, NoStream, Write] = queryId(tCId._1, tCId._2).delete
  }

}
