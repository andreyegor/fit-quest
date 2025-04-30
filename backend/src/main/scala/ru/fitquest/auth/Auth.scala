package ru.fitquest.auth

import cats.data.OptionT
import cats.effect.{Sync, Concurrent}
import cats.implicits._
import cats.data.EitherT

import ru.fitquest.core.database.UserTable
import ru.fitquest.core.structures.user.*
import ru.fitquest.core.types.*

trait Auth[F[_]](userTable: UserTable[F]) {
  def apply(rawUser: RawUser): EitherT[F, String, User]
}

object Auth:
  def impl[F[_]: Sync](userTable: UserTable[F]): Auth[F] =
    new Auth[F](userTable):
      override def apply(
          rawUser: RawUser
      ): EitherT[F, String, User] =
        for {
          user <- userTable
            .getByEmail(rawUser.email)
            .toRight("Email is invalid")
          _ <- EitherT.cond[F](user.verify(rawUser), (), "Password is invalid")

        } yield user
