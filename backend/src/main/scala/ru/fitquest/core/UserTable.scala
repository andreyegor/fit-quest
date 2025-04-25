package ru.fitquest.core

import cats.effect.Sync
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._

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
        VALUES (${user.uuid}, ${user.name}, ${user.email}, ${user.passhash}, ${user.googleId})
        """.update.run
          .transact(transactor)
          .void
          .handleErrorWith { e =>
            Sync[F].delay(
              println(s"Error during query execution: ${e.getMessage}")
            ) *> Sync[F].raiseError(e)
          }

      override def update(user: User): F[Unit] =
        sql"""
        UPDATE users
        SET name = ${user.name}, email = ${user.email}, passhash = ${user.passhash}, google_id = ${user.googleId}
        WHERE uuid = ${user.uuid}
        """.update.run
          .transact(transactor)
          .void

      override def findDuplicates(user: User): F[Option[String]] =
        sql"""
        SELECT 
            CASE 
            WHEN EXISTS (SELECT 1 FROM users WHERE name = ${user.name}) THEN 'User with this name already exists'
            WHEN EXISTS (SELECT 1 FROM users WHERE email = ${user.email}) THEN 'User with this email already exists'
            ELSE NULL
            END
        """
          .query[Option[String]]
          .unique
          .transact(transactor)
          .handleErrorWith { e =>
            Sync[F].delay(
              println(s"Error during query execution: ${e.getMessage}")
            ) *> Sync[F].raiseError(e)
          }
