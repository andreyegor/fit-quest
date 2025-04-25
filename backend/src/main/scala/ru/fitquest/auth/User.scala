package ru.fitquest.auth

import cats.Monad
import cats.data.{Kleisli, OptionT}
import cats.effect.Sync
import org.http4s.{BasicCredentials, Request}
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware

case class User(id: Long, name: String)

trait UserAuth[F[_]]:
  def authenticate(req: Request[F]): OptionT[F, User]

object UserAuth:
  def apply[F[_]: Sync]: UserAuth[F] = (req: Request[F]) =>
    val authHeader: Option[Authorization] = req.headers.get[Authorization]
    authHeader match
      case Some(Authorization(BasicCredentials(username, _))) =>
        OptionT.liftF(Sync[F].pure(User(1, username)))
      case _ =>
        OptionT.none[F, User]

def userMiddleware[F[_]: Monad](auth: UserAuth[F]): AuthMiddleware[F, User] =
  AuthMiddleware(Kleisli(req => auth.authenticate(req)))