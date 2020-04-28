package be.cetic.inah.commons.sql


import akka.dispatch.ExecutionContexts
import be.cetic.inah.commons.authentication.PasswordUtil
import be.cetic.inah.commons.database.sql.access_control.model.TokenDto
import be.cetic.inah.commons.database.sql.management.model._
import be.cetic.inah.commons.database.sql.{SchemaNames, SqlDao}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor}

class SetupSqlTest extends PasswordUtil {
  implicit val dispatcher: ExecutionContextExecutor = ExecutionContexts.global()
  SchemaNames.setNoGivenSchema
  val sqlDao = SqlDao.set(slick.jdbc.SQLiteProfile, Some("sqlite"))

  import sqlDao.driver.api._


  def setup() = {
    createSqlSchema()
    createTestData()
  }

  def createSqlSchema() = {
    Await.result(
      for {
        _ <- sqlDao.db.run(sqlDao.driver.api.DBIO.sequence(sqlDao.createSchemaStatements))
        _ <- sqlDao.db.run(sqlDao.setup)
      } yield {},
      Duration.Inf
    )
  }

  def createTestData() = {
    val projects = ProjectDto(Some("1"), "Demo", "PENDING", 1585730581, Some(1))
    val tokens = TokenDto(Some(1), "token 1", "UP", 1009026, 1009035)
    val resourcesDTO = ResourceDto(Some(1), "resource 1", "type resource 1", "resource 1 content", Some("created"), Some(1009026))
    val projectResourceDto = ProjectResourceDto("1", 1)
    val pwd = hashPassword("abc")
    val user = UserDto("test@gmail.com", "test user", pwd, "USER")
    val userDataSource = UserDto("test.datasource@gmail.com", "test user", pwd, "DATASOURCE")
    val userAdmin = UserDto("test.admin@gmail.com", "admin user", pwd, "ADMIN")
    val projectUserDto = ProjectUserDto("1", "test@gmail.com")
    val dataSourceDto = DatasourceDto(Some(1), "data source 1", "inah.cetic.be", "UP", 1009026)
    val dataSource2Dto = DatasourceDto(Some(2), "data source 2", "inah.cetic.be", "UP", 12009300)
    val projectDataSourceDto = ProjectDatasourceDto("1", 1, Some(1585824124))

    val setup = DBIO.seq(
      sqlDao.managementDao.ProjectDao.create(projects),
      sqlDao.accessControlDao.TokensDao.create(tokens),
      sqlDao.managementDao.ResourceDao.create(resourcesDTO),
      sqlDao.managementDao.UserDao.create(user),
      sqlDao.managementDao.UserDao.create(userAdmin),
      sqlDao.managementDao.UserDao.create(userDataSource),
      sqlDao.managementDao.UserDao.update(user),
      sqlDao.managementDao.UserDao.update(userAdmin),
      sqlDao.managementDao.UserDao.update(userDataSource),
      sqlDao.managementDao.DatasourceDao.create(dataSourceDto),
      sqlDao.managementDao.DatasourceDao.create(dataSource2Dto),
      sqlDao.managementDao.ProjectUserDao.create(projectUserDto),
      sqlDao.managementDao.ProjectResourceDao.create(projectResourceDto),
      sqlDao.managementDao.ProjectDatasourceDao.create(projectDataSourceDto),
    )

    Await.result(sqlDao.db.run(setup.transactionally), Duration.Inf)

  }

}
