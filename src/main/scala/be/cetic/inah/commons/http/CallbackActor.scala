package be.cetic.inah.commons.http

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, Uri}
import be.cetic.inah.commons.http.CallbackActor.SendCallback
import spray.json.{DefaultJsonProtocol, JsValue}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object CallbackActor {

  case class SendCallback(url: String, content: Option[JsValue], token: Option[String])

  def props(implicit system: ActorSystem) = Props(new CallbackActor)
}

class CallbackActor(implicit system: ActorSystem) extends Actor with ActorLogging with DefaultJsonProtocol {

  implicit val dispatcher: ExecutionContextExecutor = context.dispatcher
  val http = Http()

  override def receive: Receive = {
    case SendCallback(to, content, token) => send(to, content, token)
    case m => log.error(s"$this received unhandled $m.")
  }

  private def send(to: String, content: Option[JsValue], token: Option[String]) = {
    http.singleRequest(request(to, content, token)).collect {
      case r: HttpResponse if r.status.intValue() < 300 => log.info(s"Callback successfully sent to $to")
      case r: HttpResponse => log.error(s"Couldn't send callback, received code ${r.status.intValue()}.")
      case _ => log.error("Unexpected http response.")
    }
  }

  private def request(url: String, content: Option[JsValue], token: Option[String]): HttpRequest = {
    val header = token.map(t => Authorization(OAuth2BearerToken(t)))

    var req = HttpRequest(HttpMethods.POST, Uri(url))
    header.foreach(h => req = req.withHeaders(h))
    content.foreach(js => req = req.withEntity(HttpEntity(ContentTypes.`application/json`, js.toString())))
    req
  }
}
