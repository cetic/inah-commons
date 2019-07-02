package be.cetic.inah.commons

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import be.cetic.inah.commons.service.Commons

import scala.concurrent.duration._
import scala.concurrent.ExecutionContextExecutor

object Main extends App {
   implicit val system : ActorSystem =  ActorSystem("Search")
   implicit val materializer: ActorMaterializer = ActorMaterializer()
   implicit val executionContext: ExecutionContextExecutor = system.dispatcher
   implicit val timeout: Timeout = Timeout(10.seconds)

   val routes: Route = new Commons().routes
   val interface = "localhost"
   val port = 8000
   Http().bindAndHandle(routes, interface, port)

   println(s"Listening to $interface:$port")
}
