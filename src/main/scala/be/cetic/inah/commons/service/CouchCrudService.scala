package be.cetic.inah.commons.service

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import be.cetic.inah.commons.database.couchdb._
import org.lightcouch.NoDocumentException
import spray.json.{JsValue, _}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.FiniteDuration

class CouchCrudService[T](couchConfPath: String, couchTable: String, timeoutDuration: FiniteDuration)
                         (implicit val getKey: T => String,
                          implicit val getJson: T => JsValue,
                          implicit val fromJson: JsValue => T,
                          implicit val dispatcher: ExecutionContextExecutor,
                          implicit val system: ActorSystem,
                          implicit val materializer: ActorMaterializer)
  extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val timeout: Timeout = Timeout(timeoutDuration)
  val documentActor = system.actorOf(CouchDocumentActor.props[T](couchConfPath, couchTable))


  def routes: Route =
    pathEnd {
      get {
        complete {
          val response = (documentActor ? CouchDocumentActor.ReadAllDocuments).mapTo[ReadAllSuccess]
          response.map { r =>
            r.responses.filter {
              case _: ReadSuccess => true
              case _ => false
            }.map {
              _.asInstanceOf[ReadSuccess].content
            }
          }
        }
      } ~
        post {
          entity(as[JsValue]) { doc =>
            complete {
              (documentActor ? CouchDocumentActor.CreateDocument(fromJson(doc))).mapTo[CreateSuccess]
                .map(_ => getJson(doc))
            }
          }
        } ~
        put {
          entity(as[JsValue]) { doc =>
            complete {
              (documentActor ? CouchDocumentActor.UpdateDocument(fromJson(doc))).mapTo[UpdateSuccess]
                .map(_ => getJson(doc))
            }
          }
        }
    } ~
      path(Segment) { doc =>
        get {
          complete {
            (documentActor ? CouchDocumentActor.ReadDocument(doc))
              .mapTo[ReadResponse]
              .map(handleReadResponse)
          }
        } ~
          delete {
            complete {
              val response = (documentActor ? CouchDocumentActor.DeleteDocument(doc)).mapTo[DeleteSuccess]
              response.map(_ => StatusCodes.NoContent)
            }
          }
      }


  def handleReadResponse(resp: ReadResponse): ToResponseMarshallable = {
    resp match {
      case r: ReadSuccess => StatusCodes.OK -> r.content
      case ReadFailure(k, t) if t.getClass == classOf[NoDocumentException] => StatusCodes.NoContent
      case ReadFailure(k, t) => StatusCodes.InternalServerError -> t.toString
    }
  }


}
