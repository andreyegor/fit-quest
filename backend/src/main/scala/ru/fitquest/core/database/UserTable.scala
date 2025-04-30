package ru.fitquest.core.database

import cats.effect.Sync
import cats.implicits.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*

import ru.fitquest.core.structures.user.*
import ru.fitquest.core.types.*
import cats.instances.boolean
import cats.data.EitherT
import cats.data.OptionT

trait UserTable[F[_]](transactor: Transactor[F]) {
  def add(user: User): F[Unit]
  def update(user: User): F[Unit]
  def validateUnique(user: User): EitherT[F, String, Unit]
  def getByEmail(email: Email): OptionT[F, User]
}

object UserTable:
  def impl[F[_]: Sync](transactor: Transactor[F]): UserTable[F] =
    new UserTable[F](transactor):
      override def add(user: User): F[Unit] =
        sql"""
        INSERT INTO users (user_id, name, email, password_hash, google_id)
        VALUES ( ${user.userId.value}, 
                 ${user.name.value}, 
                 ${user.email.value}, 
                 ${user.passhash.map(_.value)}, 
                 ${user.googleId.map(_.value)})
        """.update.run
          .transact(transactor)
          .void

      override def update(user: User): F[Unit] =
        sql"""
        UPDATE users
        SET name = ${user.name.value}, 
        email = ${user.email.value}, 
        password_hash = ${user.passhash.map(_.value)}, 
        google_id = ${user.googleId.map(_.value)}
        WHERE user_id = ${user.userId.value}
        """.update.run
          .transact(transactor)
          .void

      override def validateUnique(user: User): EitherT[F, String, Unit] =
        val resp = sql"""
        SELECT 
          CASE 
          WHEN EXISTS (SELECT 1 FROM users WHERE email = ${user.email.value}) THEN 'User with this email already exists'
          WHEN ${user.googleId.map(
            _.value
          )} IS NOT NULL AND EXISTS (SELECT 1 FROM users WHERE google_id = ${user.googleId
            .map(_.value)}) THEN 'User with this Google ID already exists'
          ELSE NULL
          END
        """
          .query[Option[String]]
          .unique
          .transact(transactor)

        EitherT(resp.map(_.toLeft(())))

      override def getByEmail(email: Email): OptionT[F, User] =
        val resp = sql"""
            SELECT user_id, name, email, password_hash, google_id
            FROM users
            WHERE email = ${email.value}
          """
          .query[User]
          .option
          .transact(transactor)
        OptionT(resp)
