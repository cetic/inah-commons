package be.cetic.inah.commons.database.sql.management

import java.sql.Blob

import be.cetic.inah.commons.database.sql.management.model._
import spray.json.{DefaultJsonProtocol, DeserializationException, JsNull, JsValue, RootJsonFormat}

import scala.util.{Failure, Try}


trait SqlManagementJsonProtocol extends DefaultJsonProtocol {
  implicit val datasourceDtoJsonFormat: RootJsonFormat[DatasourceDto] = jsonFormat5(DatasourceDto)
  implicit val projectDatasourceJsonFormat: RootJsonFormat[ProjectDatasourceDto] = jsonFormat3(ProjectDatasourceDto)
  implicit val userDatasourceJsonFormat: RootJsonFormat[UserDatasourceDto] = jsonFormat2(UserDatasourceDto)
  implicit val projectDtoJsonFormat: RootJsonFormat[ProjectDto] = jsonFormat8(ProjectDto)
  implicit val projectResourceJsonFormat: RootJsonFormat[ProjectResourceDto] = jsonFormat2(ProjectResourceDto)
  implicit val resourceDtoJsonFormat: RootJsonFormat[ResourceDto] = jsonFormat6(ResourceDto)
  implicit val userDtoJsonFormat: RootJsonFormat[UserDto] = jsonFormat4(UserDto)
  implicit val feedbackDtoJsonFormat: RootJsonFormat[FeedbackDto] = jsonFormat5(FeedbackDto)
  implicit val userDetailJsonFormat: RootJsonFormat[UserDetailsDto] = jsonFormat7(UserDetailsDto)

  implicit object ManagementResourceJsonFormat extends RootJsonFormat[ManagementResource] {
    override def read(json: JsValue): ManagementResource = {
      val trials = Seq(
        Try(datasourceDtoJsonFormat.read(json)),
        Try(resourceDtoJsonFormat.read(json)),
        Try(projectDtoJsonFormat.read(json)),
        Try(projectResourceJsonFormat.read(json)),
        Try(projectDatasourceJsonFormat.read(json)),
        Try(userDatasourceJsonFormat.read(json)),
        Try(userDtoJsonFormat.read(json)),
        Try(userDetailJsonFormat.read(json)),
        Try(feedbackDtoJsonFormat.read(json))
      )
      trials.find(_.isSuccess)
        .getOrElse(Failure[ManagementResource](new DeserializationException(s"Cannot read $json.")))
        .get
    }

    override def write(obj: ManagementResource): JsValue = {
      obj match {
        case o: FeedbackDto => datasourceDtoJsonFormat.write(o)
        case o: ResourceDto => resourceDtoJsonFormat.write(o)
        case o: ProjectDto => projectDtoJsonFormat.write(o)
        case o: ProjectDatasourceDto => projectDatasourceJsonFormat.write(o)
        case o: UserDatasourceDto => userDatasourceJsonFormat.write(o)
        case o: FeedbackDto => feedbackDtoJsonFormat.write(o)
        case o: ProjectResourceDto => projectResourceJsonFormat.write(o)
        case o: UserDto => userDtoJsonFormat.write(o)
        case o: UserDetailsDto => userDetailJsonFormat.write(o)
        case m => throw DeserializationException(s"Support for $m not implemented.")
      }
    }

  }

}
