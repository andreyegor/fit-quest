package ru.fitquest.backend.core.database

import cats.effect.Sync
import cats.implicits.*
import cats.data.EitherT
import cats.data.OptionT
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*

import ru.fitquest.backend.core.structures.user.*
import ru.fitquest.backend.core.structures.session.*

trait SessionsTable[F[_]](transactor: Transactor[F]) {
  def add(session: Session): F[Unit]
  def revoke(userId: UserId): F[Int] // например, при logout
  def revokeAndGet(hash: RefreshTokenHash): OptionT[F, Session]
}

object SessionsTable:
  def impl[F[_]: Sync](transactor: Transactor[F]): SessionsTable[F] =
    new SessionsTable[F](transactor):
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

      override def revoke(userId: UserId): F[Int] =
        sql"""
          UPDATE sessions
          SET revoked = true
          WHERE uid = ${userId.value}
        """.update.run
          .transact(transactor)

      override def revokeAndGet(hash: RefreshTokenHash): OptionT[F, Session] =
        OptionT {
          sql"""
            UPDATE sessions
            SET is_revoked = true
            WHERE token_hash = ${hash.value} AND is_revoked = false
            RETURNING session_id, user_id, token_hash, issued_at, expires_at, device_info, is_revoked
          """
            .query[Session]
            .option
            .transact(transactor)
        }
