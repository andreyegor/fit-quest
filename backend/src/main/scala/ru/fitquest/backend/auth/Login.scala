package ru.fitquest.backend.auth

import cats.effect.Sync
import cats.effect.Clock
import cats.syntax.all.*
import cats.data.EitherT
import java.time.Instant
import java.util.UUID

import ru.fitquest.backend.core.database.{UserTable, SessionsTable}
import ru.fitquest.backend.core.structures.session.*
import ru.fitquest.backend.core.structures.user.*
import ru.fitquest.backend.core.structures.user.UserRequest
import ru.fitquest.backend.core.security.GenerateTokens


trait Login[F[_]] {
  def apply(rawUser: UserRequest): EitherT[F, String, Tokens]
}

object Login:
  def impl[F[_]: Sync](
      generateTokens: GenerateTokens,
      authenticate: Authenticate[F],
      sessionsTable: SessionsTable[F]
  ): Login[F] = new Login[F] {
    override def apply(rawUser: UserRequest): EitherT[F, String, Tokens] = for {
      user <- authenticate.authenticateUser(rawUser)
      now <- EitherT.liftF(Clock[F].realTimeInstant)
      access = generateTokens.generateAccessToken(user)
      refresh = generateTokens.generateRefreshToken
      session = Session.create(user.userId, refresh, now)
      _ <- EitherT.liftF(sessionsTable.add(session))
    } yield Tokens(access, refresh)

    // override protected def auth(
    //     rawUser: UserRequest
    // ): EitherT[F, String, User] =
    //   for {
    //     user <- userTable
    //       .getByEmail(rawUser.email)
    //       .toRight("Email is invalid")
    //     _ <- EitherT.cond[F](user.verify(rawUser), (), "Password is invalid")

    //   } yield user
  }
