package ru.fitquest.routes

import ru.fitquest.identity
import ru.fitquest.core.User

import cats.data.EitherT
import cats.effect.{Concurrent, Async}
import cats.implicits.*
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.AuthedRoutes
import org.typelevel.log4cats.slf4j.Slf4jLogger
import ru.fitquest.identity.Register
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe._
import org.http4s.Response
import org.http4s.server.middleware.Logger

object IdentityRoutes:
  def registerRoute[F[_]: Concurrent](R: identity.Register[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl.*
    HttpRoutes.of[F] { case req @ POST -> Root / "identity" / "new" =>
      // собираем всю логику в EitherT[F, Response[F], Response[F]]
      (for {
        userIn <- EitherT.liftF(req.as[UserIn])
        user <- EitherT.fromEither[F](userIn.toUser()).leftSemiflatMap(msg => Conflict(msg))
        t <- EitherT(R(user)).leftSemiflatMap(msg => Conflict(msg))
        resp <- EitherT.liftF(Created("User registered successfully"))
      } yield resp).value.flatMap {
        case Right(response) => response.pure[F]
        case Left(errorResponse) => errorResponse.pure[F]
      }
    }

  def registerRouteLogger[F[_]: Async](R: identity.Register[F]): HttpRoutes[F] =
      Logger.httpRoutes[F](logHeaders = true, logBody = true)(registerRoute(R))

case class UserIn(
    name: String,
    email: String,
    password: Option[String],
    googleId: Option[String]
):
  def toUser(): Either[String, User] =
    User.createWithPassword(name, email, password, googleId)

object UserIn:
  given userInDecoder: Decoder[UserIn] = deriveDecoder[UserIn]
  given userInEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, UserIn] =
    jsonOf[F, UserIn]
