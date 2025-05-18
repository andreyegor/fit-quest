package ru.fitquest.backend.auth

import cats.data.Kleisli
import cats.effect.Concurrent
import cats.syntax.all._
import cats.data.OptionT
import org.http4s.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.AuthMiddleware
import org.http4s.circe.jsonDecoder
import io.circe.syntax.*
import io.circe.Json

import ru.fitquest.backend.core.security.Cookie
import ru.fitquest.backend.core.structures.session.AccessToken
import ru.fitquest.backend.core.structures.user.User

object Middleware:

  def apply[F[_]: Concurrent](auth: Authenticate[F]): AuthMiddleware[F, User] =
    val dsl = Http4sDsl[F]; import dsl.*

    val authUser = Kleisli { (req: Request[F]) =>
      for {
        maybeIn <- Cookie
          .fromRequest(req, Cookie.Name.AccessToken)
          .fold(
            req
              .as[Json]
              .map(json => json.hcursor.get[String]("accessToken").toOption)
          )(token => Some(token).pure[F])
        eitherToken = maybeIn match {
          case Some(t) => Right(AccessToken(t))
          case None    => Left("Missing access token")
        }
        res <- eitherToken match {
          case Right(token) => auth.authenticateToken(token).value
          case Left(err)    => Left[String, User](err).pure[F]
        }
      } yield res
    }

    val onFailure: AuthedRoutes[String, F] = Kleisli { _ =>
      val challenge = Challenge("Bearer", "", Map.empty)
      val wwwAuth = headers.`WWW-Authenticate`(challenge)
      OptionT.liftF(Unauthorized(wwwAuth))
    }

    AuthMiddleware(authUser, onFailure)
