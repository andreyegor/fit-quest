package ru.fitquest.core.database

import cats.effect.Sync
import cats.implicits.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*

import ru.fitquest.core.structures.User
import ru.fitquest.core.types._


trait UserTable[F[_]](transactor: Transactor[F]) {
  def add(user: User): F[Unit]
  def update(user: User): F[Unit]
  def findDuplicates(user: User): F[Option[String]]

}

object UserTable:
  def impl[F[_]: Sync](transactor: Transactor[F]): UserTable[F] =
    new UserTable[F](transactor):
      override def add(user: User): F[Unit] =
        sql"""
        INSERT INTO users (uuid, name, email, passhash, google_id)
        VALUES (${user.uuid}, ${user.name.value}, ${user.email.value}, ${user.passhash
            .map(_.value)}, ${user.googleId.map(_.value)})
        """.update.run
          .transact(transactor)
          .void

      override def update(user: User): F[Unit] =
        sql"""
        UPDATE users
        SET name = ${user.name.value}, email = ${user.email.value}, passhash = ${user.passhash
            .map(_.value)}, google_id = ${user.googleId.map(_.value)}
        WHERE uuid = ${user.uuid}
        """.update.run
          .transact(transactor)
          .void

      override def findDuplicates(user: User): F[Option[String]] =
        sql"""
        SELECT 
            CASE 
            WHEN EXISTS (SELECT 1 FROM users WHERE email = ${user.email.value}) THEN 'User with this email already exists'
            ELSE NULL
            END
        """
          .query[Option[String]]
          .unique
          .transact(transactor)
