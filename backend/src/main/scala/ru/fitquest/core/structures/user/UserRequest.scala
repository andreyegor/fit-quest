package ru.fitquest.core.structures.user

import cats.effect.Concurrent
import cats.syntax.all.*
import io.circe.Decoder
import io.circe.generic.semiauto.*
import org.http4s.EntityDecoder
import org.http4s.circe.*

import ru.fitquest.core.types._

case class UserRequest(
    email: Email,
    password: Option[Password],
    googleId: Option[GoogleId]
)

object UserRequest:
  def apply(
      email: String,
      password: Option[String],
      googleId: Option[String]
  ): Either[String, UserRequest] =
    (password, googleId) match
      case (None, None) =>
        Left("At least one of passhash or googleId must be defined")
      case _ =>
        for {
          e <- Email(email)
          p <- password.traverse(Password(_))
          g <- googleId.traverse(GoogleId(_))
        } yield UserRequest(e, p, g)
  given rawUserDecoder: Decoder[UserRequest] = deriveDecoder[UserRequest]
  given rawUserEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, UserRequest] =
    jsonOf[F, UserRequest]
