package be.cetic.inah.commons.database.sql

import be.cetic.inah.commons.database.sql.management.model._
import be.cetic.inah.commons.database.sql.management.serialization._
import spray.json.{DeserializationException, JsValue, RootJsonFormat}

import scala.util.{Failure, Try}

trait ManagementResource

trait SqlJsonProtocol extends DatasourceJsonProtocol with ProjectDtoJsonProtocol with ProjectDatasourcesJsonProtocol
with ProjectResourcesJsonProtocol with ResourceDtoJsonProtocol with UserDtoJsonProtocol{

  implicit object ManagementResourceJsonFormat extends RootJsonFormat[ManagementResource]{
    override def read(json: JsValue): ManagementResource = {
      val trials = Seq(
        Try(datasourceDtoJsonFormat.read(json)),
        Try(resourceDtoJsonFormat.read(json)),
        Try(projectDtoJsonFormat.read(json)),
        Try(projectResourceJsonFormat.read(json)),
        Try(projectDatasourceJsonFormat.read(json)),
        Try(userDtoJsonFormat.read(json))
      )
      trials.find(_.isSuccess)
        .getOrElse(Failure[ManagementResource]( new  DeserializationException(s"Cannot read $json.")))
        .get
    }

    override def write(obj: ManagementResource): JsValue = {
      obj match {
        case o : DatasourceDto => datasourceDtoJsonFormat.write(o)
        case o : ResourceDto => resourceDtoJsonFormat.write(o)
        case o : ProjectDto =>projectDtoJsonFormat.write(o)
        case o : ProjectDatasourceDto => projectDatasourceJsonFormat.write(o)
        case o : ProjectResourceDto => projectResourceJsonFormat.write(o)
        case o : UserDto => userDtoJsonFormat.write(o)
        case m => throw  DeserializationException(s"Support for $m not implemented.")
      }
    }

  }
}
