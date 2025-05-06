package ru.fitquest.backend.core.structures.user

import java.util.UUID

import cats.effect.Concurrent
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe._

import ru.fitquest.backend.core.security.Argon2Hasher

case class User private (
    userId: UserId,
    name: Name,
    email: Email,
    passwordHash: Option[PasswordHash],
    googleId: Option[GoogleId]
):
  def verify(rawUser: UserRequest): Boolean =
    (for {
      h <- passwordHash
      p <- rawUser.password
    } yield email == rawUser.email && h.verify(p)).getOrElse(false)

object User {
  def apply(
      userId: UserId,
      name: Name,
      email: Email,
      passhash: Option[PasswordHash],
      googleId: Option[GoogleId]
  ): Either[String, User] =
    if (passhash.isEmpty && googleId.isEmpty)
      Left("At least one of passhash or googleId must be defined")
    else
      Right(new User(userId, name, email, passhash, googleId))

  def create(rawUser: NewUserRequest): Either[String, User] =
    apply(
      UserId.random,
      rawUser.name,
      rawUser.email,
      rawUser.password.map(PasswordHash.from),
      rawUser.googleId
    )
}
