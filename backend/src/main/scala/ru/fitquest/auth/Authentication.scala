package ru.fitquest.auth

import ru.fitquest.core.structures.RawUser
import ru.fitquest.core.structures.User
import ru.fitquest.core.database.UserTable

import cats.data.OptionT
import cats.effect.{Sync, Concurrent}
import cats.implicits._
import cats.data.EitherT

trait Authentication[F[_]](userTable: UserTable[F]) {
  def apply(user: RawUser): F[Either[String, Unit]]
}

object Authentication:
  def impl[F[_]: Sync](userTable: UserTable[F]): Authentication[F] =
    new Authentication[F](userTable):
      override def apply(userIn: RawUser): F[Either[String, Unit]] =
        User.fromUserIn(userIn) match {
          case Left(err) => Sync[F].pure(Left(err))
          case Right(user) =>
            OptionT(userTable.findDuplicates(user))
              .foldF(userTable.add(user).map(Right(_)))(msg =>
                Sync[F].pure(Left(msg))
              )
        }
