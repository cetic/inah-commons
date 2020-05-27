package be.cetic.inah.commons.database.sql.access_control

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import be.cetic.inah.commons.database.sql.access_control.model._
import spray.json.{DefaultJsonProtocol, DeserializationException, JsValue, RootJsonFormat}

import scala.util.{Failure, Try}

trait SqlAccessControlJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val serviceDtoJsonFormat: RootJsonFormat[ServiceDto] = jsonFormat5(ServiceDto)
  implicit val ruleDtoJsonFormat: RootJsonFormat[RuleDto] = jsonFormat6(RuleDto)
  implicit val routeDtoJsonFormat: RootJsonFormat[RouteDto] = jsonFormat4(RouteDto)
  implicit val tokenDataDtoJsonFormat: RootJsonFormat[TokenDataDto] = jsonFormat3(TokenDataDto)
  implicit val tokenDtoJsonFormat: RootJsonFormat[TokenDto] = jsonFormat5(TokenDto)
  implicit val tokenContextDtoJsonFormat: RootJsonFormat[TokenContextDto] = jsonFormat2(TokenContextDto)
  implicit val tokenRightDtoJsonFormat: RootJsonFormat[TokenRightDto] = jsonFormat2(TokenRightDto)
  implicit val rightDtoJsonFormat: RootJsonFormat[RightDto] = jsonFormat4(RightDto)
  implicit val rightPolicyJsonFormat: RootJsonFormat[RightPolicyDto] = jsonFormat2(RightPolicyDto)
  implicit val policyDtoJsonFormat: RootJsonFormat[PolicyDto] = jsonFormat6(PolicyDto)


  implicit object AccessControlResourceJsonFormat extends RootJsonFormat[AccessControlResource] {
    override def read(json: JsValue): AccessControlResource = {
      val trials = Seq(
        Try(serviceDtoJsonFormat.read(json)),
        Try(ruleDtoJsonFormat.read(json)),
        Try(routeDtoJsonFormat.read(json)),
        Try(tokenDataDtoJsonFormat.read(json)),
        Try(tokenDtoJsonFormat.read(json)),
        Try(tokenContextDtoJsonFormat.read(json)),
        Try(tokenRightDtoJsonFormat.read(json)),
        Try(rightDtoJsonFormat.read(json)),
        Try(rightPolicyJsonFormat.read(json)),
        Try(policyDtoJsonFormat.read(json))
      )
      trials.find(_.isSuccess)
        .getOrElse(Failure[AccessControlResource](new DeserializationException(s"Cannot read $json.")))
        .get
    }

    override def write(obj: AccessControlResource): JsValue = {
      obj match {
        case o: ServiceDto => serviceDtoJsonFormat.write(o)
        case o: RuleDto => ruleDtoJsonFormat.write(o)
        case o: RouteDto => routeDtoJsonFormat.write(o)
        case o: TokenDataDto => tokenDataDtoJsonFormat.write(o)
        case o: TokenDto => tokenDtoJsonFormat.write(o)
        case o: TokenContextDto => tokenContextDtoJsonFormat.write(o)
        case o: TokenRightDto => tokenRightDtoJsonFormat.write(o)
        case o: RightDto => rightDtoJsonFormat.write(o)
        case o: RightPolicyDto => rightPolicyJsonFormat.write(o)
        case o: PolicyDto => policyDtoJsonFormat.write(o)
        case m => throw DeserializationException(s"Support for $m not implemented.")
      }
    }

  }

}
