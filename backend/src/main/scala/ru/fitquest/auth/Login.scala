package ru.fitquest.auth

import cats.effect.Sync
import cats.effect.Clock
import cats.syntax.all.*
import cats.data.EitherT

import ru.fitquest.core.database.{UserTable, SessionsTable}
import ru.fitquest.core.structures.session.*
import ru.fitquest.core.structures.user.*
import ru.fitquest.core.structures.user.UserRequest
import java.time.Instant
import java.util.UUID

trait Login[F[_]] {
  def apply(rawUser: UserRequest): EitherT[F, String, Tokens]
}

object Login:
  def impl[F[_]: Sync](
      authenticate: Authenticate[F],
      sessionsTable: SessionsTable[F]
  ): Login[F] = new Login[F] {
    override def apply(rawUser: UserRequest): EitherT[F, String, Tokens] = for {
      user <- authenticate.authenticateUser(rawUser)
      now <- EitherT.liftF(Clock[F].realTimeInstant)
      access = AccessToken.generate(user)
      refresh = RefreshToken.generate
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
