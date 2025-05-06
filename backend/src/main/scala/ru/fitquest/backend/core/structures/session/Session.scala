package ru.fitquest.backend.core.structures.session

import java.util.UUID
import java.time.{Instant, Duration}
import java.time.temporal.{TemporalAmount}

import ru.fitquest.backend.core.structures.user.UserId

case class Session(
    id: SessionId,
    userId: UserId,
    tokenHash: RefreshTokenHash,
    issuedAt: Instant,
    expiresAt: Instant,
    deviceInfo: Option[String],
    isRevoked: Boolean
):
  def isValid(now: Instant): Boolean =
    isRevoked && expiresAt.compareTo(now) > 0
//now получаем так как я хочу оставить эти функции чистыми
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
