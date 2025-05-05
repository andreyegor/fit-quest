package ru.fitquest.auth

import cats.implicits._
import cats.data.EitherT
import cats.effect.Sync
import cats.effect.Clock

import ru.fitquest.core.structures.session.*
import ru.fitquest.core.security.GenerateTokens
import ru.fitquest.core.database.SessionsTable

trait Refresh[F[_]](sessionsTable: SessionsTable[F]):
  def apply(refreshToken: RefreshToken): EitherT[F, String, Tokens]

object Refresh:
  def impl[F[_]: Sync](sessionsTable: SessionsTable[F]): Refresh[F] =
    new Refresh[F](sessionsTable):
      override def apply(
          refreshToken: RefreshToken
      ): EitherT[F, String, Tokens] =
        for {
          session <- sessionsTable
            .revokeAndGet(RefreshTokenHash.from(refreshToken))
            .toRight("Token is invalid")
          now <- EitherT.liftF(Clock[F].realTimeInstant)
          _ <- EitherT
            .cond[F](session.isValid(now), None, "Token is invalid")
          access = GenerateTokens.generateAccessToken(session.userId)
          refresh = GenerateTokens.generateRefreshToken
          newSession = Session.create(session.userId, refresh, now)
          _ <- EitherT.liftF(
            sessionsTable.add(newSession)
          ) // Todo норм сообщение об ошибке?
        } yield Tokens(access, refresh)
