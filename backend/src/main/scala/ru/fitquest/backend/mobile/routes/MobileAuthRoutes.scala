package ru.fitquest.backend.mobile.routes

import cats.data.EitherT
import cats.effect.Concurrent
import cats.implicits.*
import org.http4s.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger
import org.http4s.circe.jsonEncoder
import io.circe.syntax.*

import ru.fitquest.backend.auth.{Login, Refresh}
import ru.fitquest.backend.core.structures.user.UserRequest
import ru.fitquest.backend.core.structures.session.{RefreshToken, Tokens}
import ru.fitquest.backend.core.security.Cookie

object MobileAuthRoutes {
  def apply[F[_]: Concurrent](L: Login[F], R: Refresh[F]): HttpRoutes[F] = {
    implicit val dsl = new Http4sDsl[F] {}
    import dsl.*

    HttpRoutes.of[F] {
      case req @ POST -> Root / "mobile" / "auth" / "login" =>
        {
          for {
            userReq <- req.as[UserRequest]
            result <- L(userReq).value
            resp <- result.fold(
              err => BadRequest(err),
              tokens => Ok(tokens.asJson)
            )
          } yield resp
        }.handleErrorWith { case e: org.http4s.InvalidMessageBodyFailure =>
          BadRequest("Invalid JSON body")
        }

      case req @ POST -> Root / "mobile" / "auth" / "refresh" =>
        {
          for {
            tokens <- req.as[Tokens]
            refresh = tokens.refreshToken
            result <- R(refresh).value
            resp <- result.fold(
              err => BadRequest(err),
              tokens => Ok(tokens.asJson)
            )
          } yield resp
        }.handleErrorWith { case e: org.http4s.InvalidMessageBodyFailure =>
          BadRequest("Invalid JSON body")
        }
    }
  }
}
