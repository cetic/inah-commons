package be.cetic.inah.commons.database.sql.management.model

import be.cetic.inah.commons.database.sql.management.ManagementResource
import be.cetic.inah.commons.database.sql.{Dao, DriverComponent, SchemaNames}
import slick.dbio.Effect.Write
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContextExecutor

case class DatasourceAdminDto(email: String, datasourceId: Int) extends ManagementResource


trait DatasourceAdminsDtoMultiDb extends UsersDtoMultiDb with DriverComponent with DatasourcesDtoMultiDb {

  import driver.api._

  class DatasourceAdminsDto(tag: Tag) extends Table[DatasourceAdminDto](tag, SchemaNames.managementSchemaName, "datasource_admin") {
    def email: Rep[String] = column[String]("email")

    def datasourceId: Rep[Int] = column[Int]("datasource_id")

    def pk = primaryKey("data_source_admin_pk", (email, datasourceId))

    def user = foreignKey("datasource_admin_user", email, UserDao.users)(_.email)

    def datasource = foreignKey("project_datasource_datasource_fk", datasourceId, DatasourceDao.elements)(_.id)

    def * = (email, datasourceId) <> (DatasourceAdminDto.tupled, DatasourceAdminDto.unapply)
  }


  implicit val dispatcher: ExecutionContextExecutor

  implicit object DatasourceAdminDao extends Dao[DatasourceAdminDto, (String, Int)] {
    val thisDriver = driver

    val datasourceAdmins = TableQuery[DatasourceAdminsDto]

    def create(element: DatasourceAdminDto): DBIOAction[DatasourceAdminDto, NoStream, Write] = {
      (datasourceAdmins += element).map(_ => element)
    }

    def update(element: DatasourceAdminDto): DBIOAction[DatasourceAdminDto, NoStream, Write] = {
      datasourceAdmins.insertOrUpdate(element).map { _ => element }
    }

    private def queryId(email: String, sourceId: Int) = datasourceAdmins.filter(pc => pc.email === email && pc.datasourceId === sourceId)

    private def queryByProjectId(email: String) = datasourceAdmins.filter(pc => pc.email === email)

    private def queryBySourceId(sourceId: Int) = datasourceAdmins.filter(pc => pc.datasourceId === sourceId)

    def readBySource(id: Int): FixedSqlStreamingAction[Seq[DatasourceAdminDto], DatasourceAdminDto, Effect.Read] = queryBySourceId(id).result

    def readByProject(id: String): FixedSqlStreamingAction[Seq[DatasourceAdminDto], DatasourceAdminDto, Effect.Read] = queryByProjectId(id).result

    def readAll: FixedSqlStreamingAction[Seq[DatasourceAdminDto], DatasourceAdminDto, Effect.Read] = datasourceAdmins.result

    def delete(email: String, sourceId: Int): FixedSqlAction[Int, NoStream, Write] = queryId(email, sourceId).delete

    def queryId(id: (String, Int)) = datasourceAdmins.filter(pc => pc.email === id._1 && pc.datasourceId === id._2)

    override def read(id: (String, Int)) = queryId(id).result

    override def delete(id: (String, Int)) = queryId(id).delete
  }

}

