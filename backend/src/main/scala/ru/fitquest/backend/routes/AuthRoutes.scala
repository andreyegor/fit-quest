package ru.fitquest.backend.routes

import cats.data.EitherT
import cats.effect.Concurrent
import cats.implicits.*
import org.http4s.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger

import ru.fitquest.backend.auth.{Login, Refresh}
import ru.fitquest.backend.core.structures.user.UserRequest
import ru.fitquest.backend.core.structures.session.{RefreshToken, Tokens}
import ru.fitquest.backend.core.security.Cookie

object AuthRoutes {
  def apply[F[_]: Concurrent](
      L: Login[F],
      R: Refresh[F]
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
              tokens => createTokenResponse(tokens, "auth/refresh/")
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
            tokens => createTokenResponse(tokens, "auth/refresh/")
          )
      }
  }

  private def createTokenResponse[F[_]: Concurrent](
      tokens: Tokens,
      refreshPath: String
  )(implicit dsl: Http4sDsl[F]): F[Response[F]] = {
    import dsl._
    val accessCookie = Cookie.fromAcessToken(tokens.accessToken)
    val refreshCookie =
      Cookie.fromRefreshToken(tokens.refreshToken, refreshPath)
    Ok().map(_.addCookie(accessCookie).addCookie(refreshCookie))
  }
}
