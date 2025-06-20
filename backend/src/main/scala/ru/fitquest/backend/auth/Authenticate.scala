package ru.fitquest.backend.auth

import cats.implicits._
import cats.data.{OptionT, EitherT}
import cats.effect.{Sync, Concurrent}

import ru.fitquest.backend.core.database.UserTable
import ru.fitquest.backend.core.structures.user.*
import ru.fitquest.backend.core.structures.session.AccessToken
import ru.fitquest.backend.core.security.GenerateTokens

trait Authenticate[F[_]](userTable: UserTable[F]) {
  def authenticateUser(rawUser: UserRequest): EitherT[F, String, User]
  def authenticateToken(acessToken: AccessToken): EitherT[F, String, User]
}

object Authenticate:
  def impl[F[_]: Sync](
      generateTokens: GenerateTokens,
      userTable: UserTable[F]
  ): Authenticate[F] =
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
            .fromOption(generateTokens.decodeAccessToken(acessToken))
            .toRight("Token is invalid")
          user <- userTable.getByUserId(userId).toRight("User not found")
        } yield user
