package ru.fitquest.auth

import cats.data.Kleisli
import cats.effect.Concurrent
import cats.syntax.all._
import cats.data.OptionT
import org.http4s.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.AuthMiddleware

import ru.fitquest.core.security.Cookie
import ru.fitquest.core.structures.session.AccessToken
import ru.fitquest.core.structures.user.User

object Middleware:

  def apply[F[_]: Concurrent](auth: Authenticate[F]): AuthMiddleware[F, User] =
    val dsl = Http4sDsl[F]; import dsl.*

    val authUser = Kleisli { (req: Request[F]) =>
      Cookie.fromRequest(req, Cookie.Name.AccessToken)
        .map(AccessToken(_))
        .fold(
          Concurrent[F].pure(Left("Missing access token"))
        )(auth.authenticateToken(_).value)
    }

    val onFailure: AuthedRoutes[String, F] = Kleisli { _ =>
      val challenge = Challenge("Bearer", "", Map.empty)
      val wwwAuth  = headers.`WWW-Authenticate`(challenge)
      OptionT.liftF(Unauthorized(wwwAuth))
    }

    AuthMiddleware(authUser, onFailure)
