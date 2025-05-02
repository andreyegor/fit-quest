package ru.fitquest.routes

import cats.data.EitherT
import cats.effect.Concurrent
import cats.implicits.*
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.slf4j.Slf4jLogger

import ru.fitquest.auth.*
import ru.fitquest.core.structures.user.{NewUserRequest, UserRequest}
import ru.fitquest.core.security.Cookie
import org.http4s.headers.Location
import org.http4s.Uri

object AuthRoutes:
  def registerRoute[F[_]: Concurrent](R: Register[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl.*
    HttpRoutes.of[F] { case req @ POST -> Root / "users" =>
      for {
        rawNewUser <- req.as[NewUserRequest]
        result <- R(rawNewUser).value
        response <- result match {
          case Right(user) => {
            val locationHeader =
              Location(Uri.unsafeFromString(s"/api/users/${user.id}"))
            Created(user, locationHeader)
          }
          case Left(errorMessage) => Conflict(errorMessage)
        }
      } yield response
    }

  def loginRoute[F[_]: Concurrent](L: Login[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl.*
    HttpRoutes.of[F] { case req @ POST -> Root / "user" / "login" =>
      for {
        rawUser <- req.as[UserRequest]
        result <- L(rawUser).value
        response <- result match {
          case Right(tokens) =>
            val cookies = Cookie.fromTokens(tokens)
            Ok().map(r => cookies.foldLeft(r)((r, c) => r.addCookie(c)))
          case Left(errorMessage) =>
            BadRequest(errorMessage) // TODO Unauthorized()?
        }
      } yield response
    }
