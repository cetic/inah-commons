package be.cetic.inah.commons.database.sql.management.model

import be.cetic.inah.commons.database.sql.management.ManagementResource
import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import slick.dbio.Effect.Write
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class UserDatasourceDto(email: String, dataSourceId: Int) extends ManagementResource


trait UserDatasourcesDtoMultiDb extends UsersDtoMultiDb with DriverComponent with DatasourcesDtoMultiDb {

  import driver.api._

  class UserDatasourcesDto(tag: Tag) extends Table[UserDatasourceDto](tag, SchemaNames.managementSchemaName, "user_datasource") {
    def email: Rep[String] = column[String]("email")

    def datasourceId: Rep[Int] = column[Int]("datasource_id")


    def pk = primaryKey("user_data_source_pk", (email, datasourceId))

    def user = foreignKey("user_datasource_project_fk", email, UserDao.users)(_.email)

    def datasource = foreignKey("user_datasource_datasource_fk", datasourceId, DatasourceDao.elements)(_.id)

    def * = (email, datasourceId) <> (UserDatasourceDto.tupled, UserDatasourceDto.unapply)
  }


  implicit val dispatcher: ExecutionContextExecutor

  implicit object UserDatasourceDao extends Dao[UserDatasourceDto, (String, Int)] {
    val thisDriver = driver

    val userDatasources = TableQuery[UserDatasourcesDto]

    def create(element: UserDatasourceDto): DBIOAction[UserDatasourceDto, NoStream, Write] = {
      (userDatasources += element).map(_ => element)
    }

    def update(element: UserDatasourceDto): DBIOAction[UserDatasourceDto, NoStream, Write] = {
      userDatasources.insertOrUpdate(element).map { _ => element }
    }

    private def queryId(email: String, sourceId: Int) = userDatasources.filter(pc => pc.email === email && pc.datasourceId === sourceId)

    private def queryByProjectId(projectId: String) = userDatasources.filter(pc => pc.email === projectId)

    private def queryBySourceId(sourceId: Int) = userDatasources.filter(pc => pc.datasourceId === sourceId)

    def readBySource(id: Int): FixedSqlStreamingAction[Seq[UserDatasourceDto], UserDatasourceDto, Effect.Read] = queryBySourceId(id).result

    def readByProject(id: String): FixedSqlStreamingAction[Seq[UserDatasourceDto], UserDatasourceDto, Effect.Read] = queryByProjectId(id).result

    def readAll: FixedSqlStreamingAction[Seq[UserDatasourceDto], UserDatasourceDto, Effect.Read] = userDatasources.result

    def delete(projectId: String, sourceId: Int): FixedSqlAction[Int, NoStream, Write] = queryId(projectId, sourceId).delete

    def queryId(id: (String, Int)) = userDatasources.filter(pc => pc.email === id._1 && pc.datasourceId === id._2)

    override def read(id: (String, Int)) = queryId(id).result

    override def delete(id: (String, Int)) = queryId(id).delete
  }

}

