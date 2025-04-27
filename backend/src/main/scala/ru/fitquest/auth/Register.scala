package ru.fitquest.auth

import cats.data.{OptionT, EitherT}
import cats.effect.{Sync, Concurrent}
import cats.implicits._

import ru.fitquest.core.structures.RawUser
import ru.fitquest.core.structures.User
import ru.fitquest.core.database.UserTable

trait Register[F[_]](userTable: UserTable[F]) {
  def apply(user: RawUser): F[Either[String, Unit]]
}

object Register:
  def impl[F[_]: Sync](userTable: UserTable[F]): Register[F] =
    new Register[F](userTable):
      override def apply(userIn: RawUser): F[Either[String, Unit]] =
        User.fromUserIn(userIn) match {
          case Left(err) => Sync[F].pure(Left(err))
          case Right(user) =>
            OptionT(userTable.findDuplicates(user))
              .foldF(userTable.add(user).map(Right(_)))(msg =>
                Sync[F].pure(Left(msg))
              )
        }
