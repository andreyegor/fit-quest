package ru.fitquest.core.structures.session

import io.circe.Decoder
import doobie.util.Get
import doobie.postgres.implicits.*
import java.util.UUID

import ru.fitquest.core.structures.user.*
import ru.fitquest.core.security.Sha256Hasher
import ru.fitquest.core.security.GenerateTokens.{
  generateAccessToken,
  generateRefreshToken
}
import io.circe.Encoder

opaque type SessionId = UUID
object SessionId:
  def apply(u: UUID): SessionId = u
  def random: SessionId = UUID.randomUUID()

  extension (u: SessionId) def value: UUID = u
  given Get[SessionId] = Get[UUID].map(SessionId(_))

opaque type AccessToken = String
object AccessToken:
  def apply(t: String): AccessToken = t
  def generate(u: User): AccessToken = generateAccessToken(u)

  extension (t: AccessToken) def value: String = t
  given Encoder[AccessToken] = Encoder[String].contramap[AccessToken](_.value)

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
  def from(t: RefreshToken): RefreshTokenHash = Sha256Hasher.hash(t)
  extension (h: Passhash)
    def verify(t: RefreshToken) = Sha256Hasher.verify(h, t)

  extension (t: RefreshTokenHash) def value: String = t
  // given Conversion[RefreshToken, String] = _.value
  given Get[RefreshTokenHash] = Get[String].map(RefreshTokenHash(_))
