package ru.fitquest.core.types

import io.circe.Decoder
import doobie.util.Get
import doobie.postgres.implicits.*
import java.util.UUID

import ru.fitquest.core.structures.user.User
import ru.fitquest.core.security.Argon2Hasher
import ru.fitquest.core.security.{generateAccessToken, generateRefreshToken}
import io.circe.Encoder

opaque type SessionId = UUID
object SessionId:
  def apply(u: UUID): SessionId = u
  def random: SessionId = UUID.randomUUID()

  extension (u: SessionId) def value: UUID = u
  given Get[SessionId] = Get[UUID].map(SessionId(_))

opaque type AcessToken = String
object AcessToken:
  def apply(t: String): AcessToken = t
  def generate(u: User): AcessToken = generateAccessToken(u)

  extension (t: AcessToken) def value: String = t
  given Encoder[AcessToken] = Encoder[String].contramap[AcessToken](_.value)

opaque type RefreshToken = String
object RefreshToken:
  def apply(t: String): RefreshToken = t
  def generate: RefreshToken = generateRefreshToken

  extension (t: RefreshToken) def value: String = t
  given Get[RefreshToken] = Get[String].map(RefreshToken(_))
    given Encoder[RefreshToken] = Encoder[String].contramap[RefreshToken](_.value)

opaque type RefreshTokenHash = String
object RefreshTokenHash:
  def apply(h: String): RefreshTokenHash = h
  def from(t: RefreshToken): RefreshTokenHash = Argon2Hasher.hash(t)
  extension (h: Passhash)
    def verify(t: RefreshToken) = Argon2Hasher.verify(h, t)

  extension (t: RefreshTokenHash) def value: String = t
  // given Conversion[RefreshToken, String] = _.value
  given Get[RefreshTokenHash] = Get[String].map(RefreshTokenHash(_))
