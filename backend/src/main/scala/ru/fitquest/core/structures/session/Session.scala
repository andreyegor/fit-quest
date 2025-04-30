package ru.fitquest.core.structures.session

import ru.fitquest.core.types.*
import java.util.UUID
import java.time.{Instant, Duration}
import java.time.temporal.{TemporalAmount}

case class Session(
    id: SessionId,
    userId: UserId,
    tokenHash: RefreshTokenHash,
    issuedAt: Instant,
    expiresAt: Instant,
    deviceInfo: Option[String],
    isRevoked: Boolean
)

object Session:
  def create(
      userId: UserId,
      token: RefreshToken,
      now: Instant,
      ttl: TemporalAmount = Duration.ofDays(1)
  ): Session =
    new Session(
      id = SessionId.random,
      userId = userId,
      tokenHash = RefreshTokenHash.from(token),
      issuedAt = now,
      expiresAt = now.plus(ttl),
      deviceInfo = None,
      isRevoked = false
    )
