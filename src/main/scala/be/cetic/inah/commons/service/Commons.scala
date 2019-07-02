package be.cetic.inah.commons.service

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import be.cetic.inah.commons.http.cors.CorsSupport

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

class Commons()(implicit val system: ActorSystem) extends OpenApiService with CorsSupport {
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(10 seconds)

  //  val CommonsActor: ActorRef = ???

  val routes: Route = Route.seal(
    cors {
      openApiRoute
    }
  )
}
