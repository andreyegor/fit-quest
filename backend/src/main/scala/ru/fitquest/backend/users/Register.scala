package ru.fitquest.backend.users

import cats.data.{OptionT, EitherT}
import cats.effect.{Sync, Concurrent}
import cats.implicits._

import ru.fitquest.backend.core.structures.user.*
import ru.fitquest.backend.core.database.UserTable

trait Register[F[_]](userTable: UserTable[F]) {
  def apply(user: NewUserRequest): EitherT[F, String, UserResponse]
}

object Register:
  def impl[F[_]: Sync](userTable: UserTable[F]): Register[F] =
    new Register[F](userTable):
      def apply(rawNewUser: NewUserRequest): EitherT[F, String, UserResponse] =
        val user = User.create(rawNewUser)
        for {
          _ <- userTable.validateUnique(user)
          _ <- EitherT.liftF(userTable.add(user))
          resp = UserResponse.from(user)
        } yield resp
