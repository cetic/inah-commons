package be.cetic.inah.commons.authentication

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.headers.{BasicHttpCredentials, HttpChallenge, HttpCredentials}
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.{ExecutionContextExecutor, Future}

trait BasicAuthenticator extends PasswordUtil with DefaultJsonProtocol{
  implicit val dispatcher: ExecutionContextExecutor
  implicit val system: ActorSystem
  implicit val timeout: Timeout
  val userActor: ActorRef

  type AuthenticationResult = Either[HttpChallenge, User]
  val challenge = HttpChallenge("Basic", Some("Inah Api"))

  def authorizeUser(credentials: Option[HttpCredentials]): Future[AuthenticationResult] = {
    credentials match {
      case Some(creds: BasicHttpCredentials) =>
        for {
          maybeUser <- (userActor ? creds).mapTo[Option[User]]
        } yield {
          maybeUser.map(Right(_)).getOrElse(Left(challenge))
        }
      case _ => Future(Left(challenge))
    }
  }

  def authenticate: Directive1[User] = authenticateOrRejectWithChallenge(authorizeUser _)

  implicit val userJsonFormat : RootJsonFormat[User] = jsonFormat6(User)

}
