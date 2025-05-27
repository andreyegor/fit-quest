package ru.fitquest.backend.auth

import cats.implicits._
import cats.data.EitherT
import cats.effect.Sync
import cats.effect.Clock

import ru.fitquest.backend.core.structures.session.*
import ru.fitquest.backend.core.security.GenerateTokens
import ru.fitquest.backend.core.database.SessionsTable

trait Logout[F[_]](sessionsTable: SessionsTable[F]):
  def apply(refreshToken: RefreshToken): EitherT[F, String, Unit]

object Logout:
  def impl[F[_]: Sync](sessionsTable: SessionsTable[F]): Logout[F] =
    new Logout[F](sessionsTable):
      override def apply(
          refreshToken: RefreshToken
      ): EitherT[F, String, Unit] =
        for {
          session <- sessionsTable
            .revokeAndGet(RefreshTokenHash.from(refreshToken))
            .toRight("Token is invalid")
          now <- EitherT.liftF(Clock[F].realTimeInstant)
          _ <- EitherT
            .cond[F](session.isValid(now), None, "Token is invalid")
        } yield ()
