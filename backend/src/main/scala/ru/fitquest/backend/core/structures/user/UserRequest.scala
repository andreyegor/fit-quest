package ru.fitquest.backend.core.structures.user

import cats.effect.Concurrent
import cats.syntax.all.*
import io.circe.Decoder
import io.circe.generic.semiauto.*
import org.http4s.EntityDecoder
import org.http4s.circe.*

case class UserRequest(
    email: Email,
    password: Password
)

object UserRequest:
  def apply(
      email: String,
      password: String
  ): Either[String, UserRequest] =
    for {
      e <- Email(email)
      p <- Password(password)
    } yield UserRequest(e, p)
  given rawUserDecoder: Decoder[UserRequest] = deriveDecoder[UserRequest]
  given rawUserEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, UserRequest] =
    jsonOf[F, UserRequest]
