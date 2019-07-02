package be.cetic.inah.commons.path

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat}

trait PathSegment

case class Sharp(ref: Option[String]) extends PathSegment

object Sharp{
  def apply(): Sharp = Sharp(None)

  def apply(ref: String): Sharp = Sharp(Some(ref))
}

case object WildCard extends PathSegment

case class Segment(name: String) extends PathSegment

case class Path(segments: Seq[PathSegment])

trait PathJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport{
  import PathImplicits._
  implicit object PathJsonFormat extends JsonFormat[Path] {
    override def read(json: JsValue): Path =
      json.asInstanceOf[JsString].value.toPath

    override def write(obj: _root_.be.cetic.inah.commons.path.Path): _root_.spray.json.JsValue = JsString(toPathImplicit(obj).toString)
  }
}