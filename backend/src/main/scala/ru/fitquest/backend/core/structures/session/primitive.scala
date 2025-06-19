package ru.fitquest.backend.core.structures.session

import io.circe.Decoder
import doobie.util.Get
import doobie.postgres.implicits.*
import java.util.UUID

import ru.fitquest.backend.core.structures.user.*
import ru.fitquest.backend.core.security.RefreshTokenHasher
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

  extension (t: AccessToken) def value: String = t
  given Encoder[AccessToken] = Encoder[String].contramap[AccessToken](_.value)
    given Decoder[AccessToken] = Decoder[String].map(AccessToken(_))

opaque type RefreshToken = String
object RefreshToken:
  def apply(t: String): RefreshToken = t

  extension (t: RefreshToken) def value: String = t
  given Get[RefreshToken] = Get[String].map(RefreshToken(_))
  given Encoder[RefreshToken] = Encoder[String].contramap[RefreshToken](_.value)
  given Decoder[RefreshToken] = Decoder[String].map(RefreshToken(_))

opaque type RefreshTokenHash = String
object RefreshTokenHash:
  def apply(h: String): RefreshTokenHash = h
  def from: RefreshToken => RefreshTokenHash = RefreshTokenHasher.hash
  extension (h: RefreshTokenHash)
    def verify(t: RefreshToken) = RefreshTokenHasher.verify(h, t)

  extension (t: RefreshTokenHash) def value: String = t
  // given Conversion[RefreshToken, String] = _.value
  given Get[RefreshTokenHash] = Get[String].map(RefreshTokenHash(_))
