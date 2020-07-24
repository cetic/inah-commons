package be.cetic.inah.commons.util

import akka.http.scaladsl.model.{HttpMethod, HttpMethods, StatusCode, StatusCodes}
import akka.http.scaladsl.model.headers.HttpCredentials
import spray.json.JsValue


case class Host(host: String, description: String, openApi: Boolean = true)
case class RoutesTests (server: Server)
case class Server(host: Seq[Host], name: String, description: String, version: String, routes: Seq[RouteDescription] = Seq())  {

  def withRoutes(routes: Seq[RouteDescription]): Server = this.copy(routes = routes)

}


case class RequestDescription(pathParameters: Map[String, Any] = Map(), body: Option[JsValue] = None,
                              query: Map[String, Any] = Map(), headers: Map[String, String] = Map(),
                              credentials: Option[HttpCredentials] = None, expectedCode: StatusCode = StatusCodes.OK,
                              acceptsStream: Boolean = false, responseStream: Boolean = false, summary: String = "") {

  def withCredentials(creds: HttpCredentials): RequestDescription = this.copy(credentials = Some(creds))

  def withBody(body: JsValue): RequestDescription = this.copy(body = Some(body))

  def withQuery(query: Map[String, String]): RequestDescription = this.copy(query = query)

  def withExpectedCode(statusCode: StatusCode): RequestDescription = this.copy(expectedCode = statusCode)

  def withPathParameters(parameters: Map[String, Any]): RequestDescription = this.copy(pathParameters = parameters)

  def withHeaders(headers: Map[String, String]): RequestDescription = this.copy(headers = headers)

  def parameters: Map[String, Any] = pathParameters ++ query
}

case class RouteDescription(method: HttpMethod, path: String, summary: String = "", requests: Seq[RequestDescription] = Seq(), tags: Seq[String] = Nil) {

  def withSummary(description: String): RouteDescription = this.copy(summary = description)

  def withTags(tags: String*): RouteDescription = this.copy(tags = (this.tags ++ tags).distinct)

  def withRequest (pathParameters: Map[String, Any] = Map(), body: Option[JsValue] = None,
                   query: Map[String, Any] = Map(), headers: Map[String, String] = Map(),
                   credentials: Option[HttpCredentials] = None, expectedCode: StatusCode = StatusCodes.OK,
                   acceptsStream: Boolean = false, responseStream: Boolean = false, summary: String = "") : RouteDescription =  {

    val r = RequestDescription (pathParameters, body, query, headers, credentials, expectedCode, acceptsStream, responseStream, summary)
    this.copy(requests = requests :+ r)
  }



  def withRequest(r: RequestDescription): RouteDescription = this.copy(requests = requests :+ r)

  def withDefaultRequest(): RouteDescription = this.copy(requests = requests :+ RequestDescription())

}

object Routes {

  def post(route: String) = RouteDescription(HttpMethods.POST, route)

  def put(route: String) = RouteDescription(HttpMethods.PUT, route)

  def get(route: String) = RouteDescription(HttpMethods.GET, route)

  def delete(route: String) = RouteDescription(HttpMethods.DELETE, route)

}
