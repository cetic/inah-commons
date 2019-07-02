package be.cetic.inah.commons.service

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import spray.json._
import scala.io.Source


trait OpenApiService extends SprayJsonSupport {
  val openApiSpec: String = null

  def openApiRoute: Route = pathPrefix("openapi") {
    pathEnd {
      get {
        complete {
          val jsonString: String = if (openApiSpec == null) {
            Source.fromResource("openapi.json").getLines().mkString("")
          } else openApiSpec

          jsonString.parseJson
        }
      }
    }
  }


}
