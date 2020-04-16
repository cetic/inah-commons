package be.cetic.inah.commons.database.sql.access_control.model

import be.cetic.inah.commons.database.sql.access_control.AccessControl
import be.cetic.inah.commons.database.sql.{DriverComponent, DtoCompositeId}
import slick.dbio.Effect.{Read, Write}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor


case class RightPolicy(access: RightDto, policy: PolicyDto)

case class RightPolicyDto(accessId: Int, policyId: Int) extends DtoCompositeId

trait RightPoliciesDtoMultiDb extends DriverComponent with RightsDtoMultiDb with PoliciesDtoMultiDb with AccessControl {

  import driver.api._

  class RightPoliciesDto(tag: Tag) extends Table[RightPolicyDto](tag,  schemaName.orElse(accessControlSchemaName), "right_policies") {

    def rightId = column[Int]("right_id")

    def policyId = column[Int]("policy_id")

    def pk = primaryKey("right_policiy_pk", (rightId, policyId))

    def * = (rightId, policyId) <> (RightPolicyDto.tupled, RightPolicyDto.unapply)

    def right = foreignKey("policy_access_fk", rightId, RightsDao.rights)(_.id)

    def policy = foreignKey("policy_policy_fk", policyId, AccessPoliciesDao.policies)(_.id)
  }


  implicit val dispatcher: ExecutionContextExecutor

  object RightPoliciesDao {

    val rightPolicies = TableQuery[RightPoliciesDto]


    def create(element: RightPolicyDto): DBIOAction[RightPolicyDto, NoStream, Write] = {
      (rightPolicies += element).map(_ => element)
    }

    def update(element: RightPolicyDto): DBIOAction[Any, NoStream, Write] = rightPolicies.insertOrUpdate(element).map { _ => element}


    private def queryId(rightId: Int, policyId: Int) = rightPolicies
      .filter(e=> e.rightId=== rightId && e.policyId === policyId)

    private def queryId(rightId: Int) = rightPolicies
      .filter(e=> e.rightId===  rightId)

    def read(rightId: Int): FixedSqlStreamingAction[Seq[RightPolicyDto], RightPolicyDto, Read] = queryId(rightId).result
    def read(rightId: Int, policyId: Int): FixedSqlStreamingAction[Seq[RightPolicyDto], RightPolicyDto, Read] = queryId(rightId, policyId).result

    def readAll: FixedSqlStreamingAction[Seq[RightPolicyDto], RightPolicyDto, Read] = rightPolicies.result

    def delete(tokenId: Int): FixedSqlAction[Int, NoStream, Write] = queryId(tokenId).delete
    def delete(tokenId: Int, rightId: Int): FixedSqlAction[Int, NoStream, Write] = queryId(tokenId, rightId).delete
  }
}