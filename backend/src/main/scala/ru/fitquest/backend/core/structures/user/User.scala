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
    passwordHash: PasswordHash
):
  def verify(rawUser: UserRequest): Boolean =
    email == rawUser.email && passwordHash.verify(rawUser.password)

object User {

  def create(rawUser: NewUserRequest): User =
    apply(
      UserId.random,
      rawUser.name,
      rawUser.email,
      PasswordHash.from(rawUser.password)
    )
}
