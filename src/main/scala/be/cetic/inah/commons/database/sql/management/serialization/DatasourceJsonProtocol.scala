package be.cetic.inah.commons.database.sql.management.serialization

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import be.cetic.inah.commons.database.sql.management.model.DatasourceDto
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait DatasourceJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport{
  implicit val datasourceDtoJsonFormat : RootJsonFormat[DatasourceDto] = jsonFormat5(DatasourceDto)
}
