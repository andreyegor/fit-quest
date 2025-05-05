package ru.fitquest.auth

import cats.implicits._
import cats.data.{OptionT, EitherT}
import cats.effect.{Sync, Concurrent}

import ru.fitquest.core.database.UserTable
import ru.fitquest.core.structures.user.*
import ru.fitquest.core.structures.session.AccessToken
import ru.fitquest.core.security.GenerateTokens

trait Authenticate[F[_]](userTable: UserTable[F]) {
  def authenticateUser(rawUser: UserRequest): EitherT[F, String, User]
  def authenticateToken(acessToken: AccessToken): EitherT[F, String, User]
}

object Authenticate:
  def impl[F[_]: Sync](userTable: UserTable[F]): Authenticate[F] =
    new Authenticate[F](userTable):
      override def authenticateUser(
          rawUser: UserRequest
      ): EitherT[F, String, User] =
        for {
          user <- userTable
            .getByEmail(rawUser.email)
            .toRight("Email is invalid")
          _ <- EitherT.cond[F](user.verify(rawUser), (), "Password is invalid")

        } yield user

      override def authenticateToken(
          acessToken: AccessToken
      ): EitherT[F, String, User] =
        for {
          userId <- OptionT
            .fromOption(GenerateTokens.decodeAccessToken(acessToken))
            .toRight("Token is invalid")
          user <- userTable.getByUserId(userId).toRight("User not found")
        } yield user
