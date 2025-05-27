package ru.fitquest.backend.routes

import cats.data.EitherT
import cats.effect.Concurrent
import cats.implicits.*
import org.http4s.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger

import ru.fitquest.backend.auth.{Login, Refresh, Logout}
import ru.fitquest.backend.core.structures.user.UserRequest
import ru.fitquest.backend.core.structures.session.{RefreshToken, Tokens}
import ru.fitquest.backend.core.security.Cookie
import cats.effect.kernel.Sync
import ru.fitquest.backend.core.structures.user.User
import ru.fitquest.backend.core.structures.user.UserResponse
import ru.fitquest.backend.core.security.Cookie.emptyRefreshToken

object AuthRoutes {
  def apply[F[_]: Concurrent](
      L: Login[F],
      R: Refresh[F],
      Lo: Logout[F]
  ): HttpRoutes[F] = {
    implicit val dsl = new Http4sDsl[F] {}
    import dsl.*
    HttpRoutes
      .of[F] {
        case req @ POST -> Root / "auth" / "login" =>
          for {
            userReq <- req.as[UserRequest]
            result <- L(userReq).value
            resp <- result.fold(
              err => BadRequest(err),
              tokens => createTokenResponse(tokens)
            )
          } yield resp

        case req @ POST -> Root / "auth" / "refresh" =>
          val result: EitherT[F, String, Tokens] =
            Cookie
              .fromRequest(req, Cookie.Name.RefreshToken)
              .fold(EitherT.leftT[F, Tokens]("Refresh token not found"))(raw =>
                R(RefreshToken(raw))
              )

          result.foldF(
            err => BadRequest(err),
            tokens => createTokenResponse(tokens)
          )

        case req @ POST -> Root / "auth" / "logout" =>
          val result: EitherT[F, String, Unit] =
            Cookie
              .fromRequest(req, Cookie.Name.RefreshToken)
              .fold(EitherT.leftT[F, Unit]("Refresh token not found"))(raw =>
                Lo(RefreshToken(raw))
              )
          result.foldF(
            err => BadRequest(err),
            tokens => emptyTokenResponse
          )
      }
  }

  def statusRoutes[F[_]: Concurrent]: AuthedRoutes[User, F] =
    val dsl = new Http4sDsl[F] {}
    import dsl.*
    AuthedRoutes.of { case GET -> Root / "auth" / "status" as user =>
      Ok(UserResponse.from(user))
    }

  private def createTokenResponse[F[_]: Concurrent](
      tokens: Tokens
  )(implicit dsl: Http4sDsl[F]): F[Response[F]] = {
    import dsl._
    val accessCookie = Cookie.fromAcessToken(tokens.accessToken)
    val refreshCookie =
      Cookie.fromRefreshToken(tokens.refreshToken)
    Ok().map(_.addCookie(accessCookie).addCookie(refreshCookie))
  }

  private def emptyTokenResponse[F[_]: Concurrent](implicit
      dsl: Http4sDsl[F]
  ): F[Response[F]] = {
    import dsl._
    val accessCookie = Cookie.emptyAcessToken
    val refreshCookie = Cookie.emptyRefreshToken
    Ok().map(_.addCookie(accessCookie).addCookie(refreshCookie))
  }
}
