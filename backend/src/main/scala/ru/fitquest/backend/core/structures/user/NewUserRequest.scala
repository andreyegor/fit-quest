package ru.fitquest.backend.core.structures.user

import cats.effect.Concurrent
import cats.syntax.all.*
import io.circe.Decoder
import io.circe.generic.semiauto.*
import org.http4s.EntityDecoder
import org.http4s.circe.*

case class NewUserRequest(
    name: Name,
    email: Email,
    password: Password
)

object NewUserRequest:
  def apply(
      name: String,
      email: String,
      password: String
  ): Either[String, NewUserRequest] =
    for {
      n <- Name(name)
      e <- Email(email)
      p <- Password(password)
    } yield NewUserRequest(n, e, p)

  given rawNewUserDecoder: Decoder[NewUserRequest] =
    deriveDecoder[NewUserRequest]
  given rawNewUserEntityDecoder[F[_]: Concurrent]
      : EntityDecoder[F, NewUserRequest] =
    jsonOf[F, NewUserRequest]
