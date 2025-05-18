package ru.fitquest.backend.core.structures.session

import cats.effect.Concurrent
import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto.*
import org.http4s.{EntityEncoder, EntityDecoder}
import org.http4s.circe.*
import org.http4s.Request
import org.http4s.RequestCookie

import ru.fitquest.backend.core.security.Cookie

case class Tokens(accessToken: AccessToken, refreshToken: RefreshToken)

object Tokens:
  given tokensEncoder: Encoder[Tokens] = deriveEncoder[Tokens]
  given tokensEntityEncoder[F[_]: Concurrent]: EntityEncoder[F, Tokens] =
    jsonEncoderOf[F, Tokens]

  given tokensDecoder: Decoder[Tokens] = deriveDecoder[Tokens]
  given tokensEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, Tokens] =
    jsonOf[F, Tokens]
