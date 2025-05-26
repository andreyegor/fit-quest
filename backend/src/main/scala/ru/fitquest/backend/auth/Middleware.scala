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
      val maybeTokenFromCookie: Option[String] =
        Cookie.fromRequest(req, Cookie.Name.AccessToken)

      val maybeTokenFromHeader: Option[String] =
        req.headers
          .get[headers.Authorization]
          .collect { case headers.Authorization(Credentials.Token(AuthScheme.Bearer, t)) => t }

      val maybeToken: F[Option[String]] =
        maybeTokenFromCookie
          .orElse(maybeTokenFromHeader)
          .pure[F]

      for {
        token <- maybeToken
        eitherToken  = token.toRight("Missing access token").map(AccessToken(_))
        res <- eitherToken match {
          case Right(token) => auth.authenticateToken(token).value
          case Left(err)    => Left[String, User](err).pure[F]
        }
      } yield res
    }

    val onFailure: AuthedRoutes[String, F] = Kleisli { _ =>
      val challenge = Challenge("Bearer", "", Map.empty)
      OptionT.liftF(Unauthorized(headers.`WWW-Authenticate`(challenge)))
    }

    AuthMiddleware(authUser, onFailure)