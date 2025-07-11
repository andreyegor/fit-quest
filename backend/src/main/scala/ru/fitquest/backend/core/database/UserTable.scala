package ru.fitquest.backend.core.database

import cats.effect.Sync
import cats.implicits.*
import cats.instances.boolean
import cats.data.{EitherT, OptionT}
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*

import ru.fitquest.backend.core.structures.user.*


trait UserTable[F[_]](transactor: Transactor[F]) {
  def add(user: User): F[Unit]
  def update(user: User): F[Unit]
  def validateUnique(user: User): EitherT[F, String, Unit]
  def getByUserId(userId: UserId): OptionT[F, User]
  def getByEmail(email: Email): OptionT[F, User]
}

object UserTable:
  def impl[F[_]: Sync](transactor: Transactor[F]): UserTable[F] =
    new UserTable[F](transactor):
      override def add(user: User): F[Unit] =
        sql"""
        INSERT INTO users (user_id, name, email, password_hash)
        VALUES ( ${user.userId.value}, 
                 ${user.name.value}, 
                 ${user.email.value}, 
                 ${user.passwordHash.value})
        """.update.run
          .transact(transactor)
          .void

      override def update(user: User): F[Unit] =
        sql"""
        UPDATE users
        SET name = ${user.name.value}, 
        email = ${user.email.value}, 
        password_hash = ${user.passwordHash.value}
        WHERE user_id = ${user.userId.value}
        """.update.run
          .transact(transactor)
          .void

      override def validateUnique(user: User): EitherT[F, String, Unit] =
        val resp = sql"""
        SELECT 
          CASE 
          WHEN EXISTS (SELECT 1 FROM users WHERE email = ${user.email.value}) THEN 'User with this email already exists'
          ELSE NULL
          END
        """
          .query[Option[String]]
          .unique
          .transact(transactor)

        EitherT(resp.map(_.toLeft(())))

      override def getByUserId(userId: UserId): OptionT[F, User] =
        val resp = sql"""
            SELECT user_id, name, email, password_hash
            FROM users
            WHERE user_id = ${userId.value}
          """
          .query[User]
          .option
          .transact(transactor)
        OptionT(resp)

      override def getByEmail(email: Email): OptionT[F, User] =
        val resp = sql"""
            SELECT user_id, name, email, password_hash
            FROM users
            WHERE email = ${email.value}
          """
          .query[User]
          .option
          .transact(transactor)
        OptionT(resp)
