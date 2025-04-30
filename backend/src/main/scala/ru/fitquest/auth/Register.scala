package ru.fitquest.auth

import cats.data.{OptionT, EitherT}
import cats.effect.{Sync, Concurrent}
import cats.implicits._

import ru.fitquest.core.structures.user.*
import ru.fitquest.core.database.UserTable

trait Register[F[_]](userTable: UserTable[F]) {
  def apply(user: RawNewUser): EitherT[F, String, Unit]
}

object Register:
  def impl[F[_]: Sync](userTable: UserTable[F]): Register[F] =
    new Register[F](userTable):
      def apply(rawNewUser: RawNewUser): EitherT[F, String, Unit] =
        (for {
          user <- EitherT.fromEither[F](User.create(rawNewUser))
          _ <- userTable.validateUnique(user)
          _ <- EitherT.liftF(userTable.add(user))
        } yield ()) 
