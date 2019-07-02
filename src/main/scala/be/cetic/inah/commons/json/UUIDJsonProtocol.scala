package be.cetic.inah.commons.json

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat}

trait UUIDJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport{

  implicit object UUIDJsonFormat extends JsonFormat[UUID] {
    override def read(json: JsValue): UUID = UUID.fromString(json.asInstanceOf[JsString].value)

    override def write(obj: UUID): JsValue = JsString(obj.toString)
  }

}
