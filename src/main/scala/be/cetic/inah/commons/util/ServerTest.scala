package be.cetic.inah.commons.util

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.HttpCredentials
import akka.http.scaladsl.model.{HttpMethod, StatusCode, Uri}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.util.Timeout
import be.cetic.inah.commons.database.sql.SqlJsonProtocol
import org.scalatest.{Matchers, WordSpecLike}
import spray.json.{DefaultJsonProtocol, JsValue}
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

case class RequestBuilderParams(val testName: String, val uri: Uri, val method: HttpMethod,
                                val bodyRequest: Option[JsValue], val cred: Option[HttpCredentials], val code: StatusCode)

class ServerTest extends WordSpecLike with Matchers with ScalatestRouteTest with DefaultJsonProtocol with SqlJsonProtocol {

//  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
//  implicit val timeout: Timeout = Timeout(5.seconds)
//
//  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(3.seconds)

  def buildTestRequestParameters(routeTests: Server): List[RequestBuilderParams] = {

    val routesDescription = routeTests.routes

    var requestsTestBuilder = new ListBuffer[RequestBuilderParams]

    for (r <- routesDescription) {
      val testName = r.summary
      var newPath: String = r.path
      val method = r.method


      for (req <- r.requests) {

        val cred = req.credentials
        val bodyRequest = req.body
        //val cred = req.credentials.get
        val requestStatusCode = req.expectedCode
        val params = req.pathParameters.toList
        for ((n, v) <- params) {

          if (newPath.contains(n)) {
            newPath = newPath.replace(n.toString, v.toString).replaceAll("[{}]", "")
          }
        }

        val uri = Uri(newPath).withQuery(Uri.Query(req.query))
        requestsTestBuilder += RequestBuilderParams(testName, uri, method, bodyRequest, cred, requestStatusCode)
      }

    }
    requestsTestBuilder.toList
  }

  def performTests(routesTests: Seq[Server], routes: Route) = {


    for (r <- routesTests) {

      val result = buildTestRequestParameters(r)

      r.name should {
        for (routeTest <- result) {

          val requestBuilder = new RequestBuilder(routeTest.method)

          routeTest.testName in {

            var request = requestBuilder(routeTest.uri, routeTest.bodyRequest)
            routeTest.cred.foreach(c => request = request ~> addCredentials(c))
            request ~> routes ~> check {
              status == routeTest.code
            }
          }
        }
      }

    }

  }

}






