package ru.fitquest.core.database

import cats.effect.Sync
import cats.implicits.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*

import ru.fitquest.core.structures.user.*
import ru.fitquest.core.structures.session.Session
import ru.fitquest.core.types.*
import cats.instances.boolean
import cats.data.EitherT
import cats.data.OptionT

trait SessionTable[F[_]](transactor: Transactor[F]) {
  def add(session: Session): F[Unit]
  def revokeByUser(userId: UserId): F[Int] // например, при logout
  def findByHash(hash: RefreshTokenHash): OptionT[F, Session]
}

object SessionTable:
  def impl[F[_]: Sync](transactor: Transactor[F]): SessionTable[F] =
    new SessionTable[F](transactor):
      override def add(session: Session): F[Unit] =
        sql"""
            INSERT INTO sessions (
            session_id, user_id, token_hash, issued_at, expires_at, device_info, is_revoked
            )
            VALUES (
            ${session.id.value}, ${session.userId.value},
            ${session.tokenHash.value}, ${session.issuedAt},
            ${session.expiresAt}, ${session.deviceInfo},
            ${session.isRevoked}
            )
            """.update.run
          .transact(transactor)
          .void

      override def revokeByUser(userId: UserId): F[Int] =
        sql"""
          UPDATE sessions
          SET revoked = true
          WHERE uid = ${userId.value}
        """.update.run
          .transact(transactor)

      override def findByHash(hash: RefreshTokenHash): OptionT[F, Session] =
        OptionT {
          sql"""
            SELECT id, uid, tokenhash, issued, expires, device, revoked
            FROM sessions
            WHERE tokenhash = ${hash.value}
            AND revoked = false
          """
            .query[Session]
            .option
            .transact(transactor)
        }
