package ru.fitquest.core.structures.user

import java.util.UUID

import cats.effect.Concurrent
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe._

import ru.fitquest.core.security.Argon2Hasher

case class User private (
    userId: UserId,
    name: Name,
    email: Email,
    passhash: Option[Passhash],
    googleId: Option[GoogleId]
):
  def verify(rawUser: UserRequest): Boolean =
    (for {
      h <- passhash
      w <- rawUser.password
    } yield email == rawUser.email && Argon2Hasher.verify(h, w)).getOrElse(false)

object User {
  def apply(
      userId: UserId,
      name: Name,
      email: Email,
      passhash: Option[Passhash],
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
      rawUser.password.map(Passhash.from),
      rawUser.googleId
    )
}
