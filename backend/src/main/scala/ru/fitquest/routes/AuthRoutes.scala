package ru.fitquest.routes

import cats.data.EitherT
import cats.effect.Concurrent
import cats.implicits.*
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.slf4j.Slf4jLogger

import ru.fitquest.auth.*
import ru.fitquest.core.structures.user.{RawNewUser, RawUser}
import ru.fitquest.core.security.Argon2Hasher

object AuthRoutes:
  def registerRoute[F[_]: Concurrent](R: Register[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl.*
    HttpRoutes.of[F] { case req @ POST -> Root / "user" / "add" =>
      for {
        rawNewUser <- req.as[RawNewUser]
        result <- R(rawNewUser).value
        response <- result match {
          case Right(_)           => Created()
          case Left(errorMessage) => Conflict(errorMessage)
        }
      } yield response
    }

  def loginRoute[F[_]: Concurrent](L: Login[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl.*
    HttpRoutes.of[F] { case req @ POST -> Root / "user" / "login" =>
      for {
        rawUser <- req.as[RawUser]
        result <- L(rawUser).value
        response <- result match {
          case Right(tokens)      => Ok(tokens)
          case Left(errorMessage) => BadRequest(errorMessage) // TODO Unauthorized()?
        }
      } yield response
    }
