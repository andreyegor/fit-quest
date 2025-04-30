package ru.fitquest.auth

import cats.effect.Sync
import cats.effect.Clock
import cats.syntax.all.*
import cats.data.EitherT

import ru.fitquest.core.database.{UserTable, SessionTable}
import ru.fitquest.core.structures.session.*
import ru.fitquest.core.structures.user.*
import ru.fitquest.core.structures.user.RawUser
import ru.fitquest.core.types._
import java.time.Instant
import java.util.UUID

trait Login[F[_]] {
  def apply(rawUser: RawUser): EitherT[F, String, Tokens]
  protected def auth(rawUser: RawUser): EitherT[F, String, User]
}

object Login:
  def impl[F[_]: Sync](
      userTable: UserTable[F],
      sessionsTable: SessionTable[F]
  ): Login[F] = new Login[F] {
    override def apply(rawUser: RawUser): EitherT[F, String, Tokens] = for {
      user <- auth(rawUser)
      now <- EitherT.liftF(Clock[F].realTimeInstant)
      access = AcessToken.generate(user)
      refresh = RefreshToken.generate
      session = Session.create(user.userId, refresh, now)
      _ <- EitherT.liftF(sessionsTable.add(session))
    } yield Tokens(access, refresh)

    override protected def auth(
        rawUser: RawUser
    ): EitherT[F, String, User] =
      for {
        user <- userTable
          .getByEmail(rawUser.email)
          .toRight("Email is invalid")
        _ <- EitherT.cond[F](user.verify(rawUser), (), "Password is invalid")

      } yield user
  }
