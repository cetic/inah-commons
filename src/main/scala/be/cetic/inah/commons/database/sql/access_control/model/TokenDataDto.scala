package be.cetic.inah.commons.database.sql.access_control.model

import be.cetic.inah.commons.database.sql.access_control.AccessControlResource
import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, DtoWithId, SchemaNames}
import slick.dbio.Effect.Write
import slick.sql.FixedSqlAction

import scala.concurrent.ExecutionContextExecutor

case class TokenDataDto(id: Option[Int], key: String, value: String) extends DtoWithId[Int] with AccessControlResource

trait ContextsDtoMultiDb extends DriverComponent  {

  import driver.api._

  class DatasDto(tag: Tag) extends Table[TokenDataDto](tag,  SchemaNames.accessControlSchemaName, "contexts") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def key = column[String]("key")

    def value = column[String]("value")

    def * = (id.?, key, value) <> (TokenDataDto.tupled, TokenDataDto.unapply)
  }


  implicit val dispatcher: ExecutionContextExecutor

  implicit object ContextsDao extends Dao[TokenDataDto, Int] {
    val thisDriver = driver

    val contexts = TableQuery[DatasDto]

    private val aAutoInc = contexts returning contexts.map(_.id)

    def create(element: TokenDataDto): DBIOAction[TokenDataDto, NoStream, Write] = {
      (aAutoInc += element).map(id => element.copy(id = Some(id)))
    }

    def update(element: TokenDataDto): DBIOAction[TokenDataDto, NoStream, Write] = aAutoInc.insertOrUpdate(element).map(_=> element)


    private def queryId(id: Int) = contexts.filter(_.id === id)

    def read(id: Int)= queryId(id).result

    def readAll= contexts.result

    def delete(id: Int): FixedSqlAction[Int, NoStream, Write] = queryId(id).delete

  }

}