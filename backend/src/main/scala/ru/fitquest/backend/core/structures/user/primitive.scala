package ru.fitquest.backend.core.structures.user

import io.circe.Decoder
import doobie.util.Get
import doobie.postgres.implicits.*
import java.util.UUID
import io.circe.Encoder

import ru.fitquest.backend.core.security.PasswordHasher

opaque type UserId = UUID
object UserId:
  def apply(u: UUID): UserId = u
  def random: UserId = UUID.randomUUID()

  extension (u: UserId) def value: UUID = u
  given Conversion[UserId, UUID] = _.value
  given Encoder[UserId] = Encoder[UUID].contramap[UserId](_.value)
  given Get[UserId] = Get[UUID].map(UserId(_))

opaque type Email = String
object Email:
  def apply(e: String): Either[String, Email] =
    if e.matches("^[^@]+@[^@]+\\.[^@]+$") then Right(e)
    else Left(s"Invalid email: $e")
  extension (e: Email) def value: String = e
  given Conversion[Email, String] = _.value
  given Decoder[Email] = Decoder[String].emap(Email(_))
  given Encoder[Email] = Encoder[String].contramap[Email](_.value)
  given Get[Email] = Get[String].temap(Email(_).left.map(_.toString))

opaque type Name = String
object Name:
  def apply(n: String): Either[String, Name] =
    if n.length() < 2 then Left("Name is too short")
    else if n.length() > 20 then Left("Name is too log")
    else Right(n)
  extension (n: Name) def value: String = n
  given Conversion[Name, String] = _.value
  given Decoder[Name] = Decoder[String].emap(Name(_))
  given Encoder[Name] = Encoder[String].contramap[Name](_.value)
  given Get[Name] = Get[String].temap(Name(_).left.map(_.toString))

opaque type Password = String
object Password:
  def apply(p: String): Either[String, Password] =
    if p.length >= 8 then Right(p) else Left("Password too short")
  // extension (p: Password) def verify(h: Passhash) = PasswordHasher.verify(h, p)

  extension (p: Password) def value: String = p
  given Conversion[Password, String] = _.value
  given Decoder[Password] = Decoder[String].emap(Password(_))

opaque type PasswordHash = String
object PasswordHash:
  def apply(h: String): PasswordHash = h
  def from: Password => PasswordHash = PasswordHasher.hash
  extension (h: PasswordHash)
    def verify(p: Password): Boolean = PasswordHasher.verify(h, p)

  extension (p: PasswordHash) def value: String = p
  given Conversion[PasswordHash, String] = _.value
  given Get[PasswordHash] = Get[String].map(PasswordHash(_))

opaque type GoogleId = String
object GoogleId:
  def apply(g: String): Either[String, GoogleId] = Right(g) // TODO проверка

  extension (g: GoogleId) def value: String = g
  given Conversion[GoogleId, String] = _.value
  given Decoder[GoogleId] = Decoder[String].emap(GoogleId(_))
  given Encoder[GoogleId] = Encoder[String].contramap[GoogleId](_.value)
  given Get[GoogleId] = Get[String].temap(GoogleId(_).left.map(_.toString))
