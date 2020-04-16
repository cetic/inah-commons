package be.cetic.inah.commons.database.sql.access_control.model

import be.cetic.inah.commons.database.sql.access_control.AccessControl
import be.cetic.inah.commons.database.sql.{DriverComponent, DtoWithId}
import slick.dbio.Effect.{Read, Write}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor


case class RuleDto(id: Option[Int], operandId1: Option[Int], operandId2: Option[Int], operand1: Option[String], operand2: Option[String],
                   operator: String) extends DtoWithId[Int]


trait RulesDtoMultiDb extends DriverComponent with AccessControl {
  import driver.api._

  implicit val dispatcher: ExecutionContextExecutor

  class RulesDto(tag: Tag) extends Table[RuleDto](tag, schemaName.orElse(accessControlSchemaName), "rule_triplets") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def op1Id = column[Option[Int]]("rule_id1")

    def op2Id = column[Option[Int]]("rule_id2")

    def operand1 = column[Option[String]]("operand1")

    def operand2 = column[Option[String]]("operand2")

    def operator = column[String]("operator")

    def * = (id.?, op1Id, op2Id, operand1, operand2, operator) <> (RuleDto.tupled, RuleDto.unapply)

    def compositeOperand1 = foreignKey("rule_operand1_fk", op1Id, RulesDao.rules)(_.id)

    def compositeOperand2 = foreignKey("rule_operand2_fk", op2Id, RulesDao.rules)(_.id)

  }


  object RulesDao {
    val rules = TableQuery[RulesDto]

    private val aAutoInc = rules returning rules.map(_.id)

    def create(element: RuleDto): DBIOAction[RuleDto, NoStream, Write] = {
      (aAutoInc += element).map(id => element.copy(id = Some(id)))
    }

    def update(element: RuleDto): DBIOAction[Any, NoStream, Write] = aAutoInc.insertOrUpdate(element)
      .map { maybeId =>
        maybeId.map { id =>
          element.copy(id = Some(id))
        }
          .getOrElse(element)
      }

    private def queryId(id: Int) = rules.filter(_.id === id)

    def read(id: Int): FixedSqlStreamingAction[Seq[RuleDto], RuleDto, Read] = queryId(id).result

    def readAll: FixedSqlStreamingAction[Seq[RuleDto], RuleDto, Read] = rules.result

    def delete(id: Int): FixedSqlAction[Int, NoStream, Write] = queryId(id).delete
  }

}
