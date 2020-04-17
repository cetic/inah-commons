package be.cetic.inah.commons.database.sql.access_control.model

import be.cetic.inah.commons.database.sql.access_control.AccessControlResource
import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, DtoWithId, SchemaNames}
import slick.dbio.Effect.{Read, Write}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class PolicyDto(id: Option[Int], name: String, description: String, required: Boolean,
                     ruleId: Option[Int], routeId: Option[Int]) extends DtoWithId[Int] with AccessControlResource

trait PoliciesDtoMultiDb extends DriverComponent with RulesDtoMultiDb with RoutesDtoMultiDb {

  import driver.api._

  class PoliciesDto(tag: Tag) extends Table[PolicyDto](tag,  SchemaNames.accessControlSchemaName, "policies") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def description = column[String]("description")

    def required = column[Boolean]("required")

    def ruleId = column[Option[Int]]("rule_id")

    def routeId = column[Option[Int]]("route_id")

    def rule = foreignKey("access_policy_rule_fk", ruleId, RulesDao.rules)(_.id)

    def route = foreignKey("access_policy_route_fk", routeId, RoutesDao.routes)(_.id)

    def * = (id.?, name, description, required, ruleId, routeId) <> (PolicyDto.tupled, PolicyDto.unapply)
  }


  implicit val dispatcher: ExecutionContextExecutor

  implicit object AccessPoliciesDao extends Dao[PolicyDto,Int] {
    override val thisDriver = driver
    val policies = TableQuery[PoliciesDto]


    private val aAutoInc = policies returning policies.map(_.id)

    def create(element: PolicyDto): DBIOAction[PolicyDto, NoStream, Write] = {
      (aAutoInc += element).map(id => element.copy(id = Some(id)))
    }

    def update(element: PolicyDto): DBIOAction[PolicyDto, NoStream, Write] = aAutoInc.insertOrUpdate(element)
      .map { maybeId =>
        maybeId.map { id =>
          element.copy(id = Some(id))
        }
          .getOrElse(element)
      }

    private def queryId(id: Int) = policies.filter(_.id === id)

    def read(id: Int): FixedSqlStreamingAction[Seq[PolicyDto], PolicyDto, Read] = queryId(id).result

    def readAll: FixedSqlStreamingAction[Seq[PolicyDto], PolicyDto, Read] = policies.result

    def delete(id: Int): FixedSqlAction[Int, NoStream, Write] = queryId(id).delete
  }

}