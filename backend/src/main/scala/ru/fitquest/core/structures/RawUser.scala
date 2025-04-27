package ru.fitquest.core.structures

import cats.effect.Concurrent
import cats.syntax.all.*
import io.circe.Decoder
import io.circe.generic.semiauto.*
import org.http4s.EntityDecoder
import org.http4s.circe.*

import ru.fitquest.core.types._


case class RawUser(
  name: Name,
  email: Email,
  password: Option[Password],
  googleId: Option[GoogleId]
)

object RawUser:
  def apply(name: String, email: String, password:Option[String], googleId:Option[String]):Either[String, RawUser] = 
  (password, googleId) match
    case (None, None) => Left("At least one of passhash or googleId must be defined")
    case _ => for {
      validName <- Name(name)
      validEmail <- Email(email)
      validPassword <- password.traverse(Password(_))
      validGoogleId <- googleId.traverse(GoogleId(_))
    } yield RawUser(validName, validEmail, validPassword, validGoogleId)
  given userInDecoder: Decoder[RawUser] = deriveDecoder[RawUser]
  given userInEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, RawUser] =
    jsonOf[F, RawUser]
