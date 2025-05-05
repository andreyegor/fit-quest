package ru.fitquest.core.structures.user

import cats.effect.Concurrent
import cats.syntax.all.*
import io.circe.Decoder
import io.circe.generic.semiauto.*
import org.http4s.EntityDecoder
import org.http4s.circe.*

case class NewUserRequest(
    name: Name,
    email: Email,
    password: Option[Password],
    googleId: Option[GoogleId]
)

object NewUserRequest:
  def apply(
      name: String,
      email: String,
      password: Option[String],
      googleId: Option[String]
  ): Either[String, NewUserRequest] =
    (password, googleId) match
      case (None, None) =>
        Left("At least one of passhash or googleId must be defined")
      case _ =>
        for {
          n <- Name(name)
          e <- Email(email)
          p <- password.traverse(Password(_))
          g <- googleId.traverse(GoogleId(_))
        } yield NewUserRequest(n, e, p, g)
  given rawNewUserDecoder: Decoder[NewUserRequest] = deriveDecoder[NewUserRequest]
  given rawNewUserEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, NewUserRequest] =
    jsonOf[F, NewUserRequest]
