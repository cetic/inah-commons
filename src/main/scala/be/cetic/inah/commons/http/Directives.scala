package be.cetic.inah.commons.http

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.headers.HttpCredentials
import akka.http.scaladsl.model.{HttpHeader, HttpMethod, RemoteAddress, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.BasicDirectives.{extractRequestContext, provide}
import akka.http.scaladsl.server.directives.FutureDirectives.onComplete
import akka.http.scaladsl.server.{Directive, Directive1}
import spray.json.{JsArray, JsObject, JsString, JsValue}

import scala.collection.immutable
import scala.util.Success


object Directives extends SprayJsonSupport {

  case class AuthorizationHeader(token: Option[String], apiId: Option[String], redirect:Option[String])

  case class RequestData(parameters: Option[JsObject], payload: Option[JsValue])

  def extractEntity: Directive1[Option[JsValue]] = {
    val um = as[JsValue]
    extractRequestContext.flatMap[Tuple1[Option[JsValue]]] { ctx ⇒
      import ctx.{executionContext, materializer}
      onComplete(um(ctx.request)) flatMap {
        case Success(value) ⇒ provide(Some(value))
        case _ => provide(None)
      }
    }
  }


  def extractParameters: Directive[Tuple1[Option[JsObject]]] = parameterSeq.flatMap { params =>
    val pgroups = params.groupBy { case (k, v) => k }
      .map { case (k, v) =>
        val jsValue = if (v.size == 1) {
          JsString(v.head._2)
        } else {
          val elements = v.map(c => JsString(c._2))
          JsArray(elements.toVector)
        }
        k -> jsValue
      }

    val paramJson = JsObject(pgroups)
    if (params.isEmpty) provide(None) else provide(Some(paramJson))
  }

  def extractHeaders: Directive[Tuple1[immutable.Seq[HttpHeader]]] = extractRequestContext.flatMap(ctx => provide(ctx.request.headers))

  def extractPath: Directive[Tuple1[Uri.Path]] = extractRequestContext.flatMap(ctx => provide(ctx.unmatchedPath))

  def extractMethod: Directive[Tuple1[HttpMethod]] = extractRequestContext.flatMap(ctx => provide(ctx.request.method))

  def extractToken: Directive[Tuple1[Option[HttpHeader]]] = extractHeaders.flatMap(hs => provide(hs.find(h => h.is("x-token"))))

  def extractApiId: Directive[Tuple1[Option[HttpHeader]]] = extractHeaders.flatMap(hs => provide(hs.find(h => h.is("x-api-id"))))

  def extractRedirect: Directive[Tuple1[Option[HttpHeader]]] = extractHeaders.flatMap(hs => provide(hs.find(h => h.is("x-redirect"))))


  def extractRequestData: Directive[Tuple1[RequestData]] = (extractEntity & extractParameters).tflatMap(dpar => provide(RequestData(dpar._2, dpar._1)))

  def extractAuthorizationHeader: Directive[Tuple1[AuthorizationHeader]] =
    (extractToken & extractApiId & extractRedirect).tflatMap(ta => provide(AuthorizationHeader(ta._1.map(_.value()), ta._2.map(_.value), ta._3.map(_.value))))

  def extractContextData: Directive[(HttpMethod, Uri.Path, immutable.Seq[(String, String)], Option[JsValue], immutable.Seq[HttpHeader], RemoteAddress, Option[HttpCredentials])] =
    extractMethod & extractPath & parameterSeq & extractEntity & extractHeaders & extractClientIP & extractCredentials

  def extractAuthorizationContext: Directive[(HttpMethod, Uri.Path, RequestData, AuthorizationHeader, RemoteAddress)] = extractMethod & extractPath & extractRequestData & extractAuthorizationHeader & extractClientIP

}
