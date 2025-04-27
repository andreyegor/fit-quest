package ru.fitquest.core.structures

import java.util.UUID

import cats.effect.Concurrent
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe._

import ru.fitquest.core.types._
import ru.fitquest.core.security.PasswordHasher

case class User private (
    uuid: UUID,
    name: Name,
    email: Email,
    passhash: Option[Passhash],
    googleId: Option[GoogleId]
)

object User {
  def apply(
      uuid: UUID,
      name: Name,
      email: Email,
      passhash: Option[Passhash],
      googleId: Option[GoogleId]
  ): Either[String, User] =
    if (passhash.isEmpty && googleId.isEmpty)
      Left("At least one of passhash or googleId must be defined")
    else
      Right(new User(uuid, name, email, passhash, googleId))

  def create(
       name: Name,
      email: Email,
      passhash: Option[Passhash],
      googleId: Option[GoogleId]
  ): Either[String, User] =
    apply(UUID.randomUUID, name, email, passhash, googleId)

  def fromUserIn(userIn: RawUser): Either[String, User] =
    create(
      userIn.name,
      userIn.email,
      userIn.password.map(PasswordHasher.hash),
      userIn.googleId
    )
}
