package ru.fitquest.core

import java.util.UUID
import java.security.MessageDigest
import cats.effect.Concurrent
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe._

case class User private (
    uuid: UUID,
    name: String,
    email: String,
    passhash: Option[String],
    googleId: Option[String]
)

object User {
  def apply(
      uuid: UUID,
      name: String,
      email: String,
      passhash: Option[String],
      googleId: Option[String]
  ): Either[String, User] =
    if (passhash.isEmpty && googleId.isEmpty)
      Left("At least one of passhash or googleId must be defined")
    else
      Right(new User(uuid, name, email, passhash, googleId))

  def create(
      name: String,
      email: String,
      passhash: Option[String],
      googleId: Option[String]
  ): Either[String, User] =
    apply(UUID.randomUUID, name, email, passhash, googleId)

  def createWithPassword(
      name: String,
      email: String,
      password: Option[String],
      googleId: Option[String]
  ): Either[String, User] =
    create(name, email, password.collect(p => hashPassword(p)), googleId)

  private def hashPassword(password: String): String = {
    val digest = MessageDigest.getInstance("SHA-256")
    digest.digest(password.getBytes("UTF-8")).map("%02x".format(_)).mkString
  }
}
