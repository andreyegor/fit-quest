package ru.fitquest.core.structures.session

import cats.effect.Concurrent
import io.circe.Encoder
import io.circe.generic.semiauto.*
import org.http4s.EntityEncoder
import org.http4s.circe.*
import org.http4s.Request
import org.http4s.RequestCookie

import ru.fitquest.core.security.Cookie


case class Tokens(acessToken: AccessToken, refreshToken: RefreshToken)


object Tokens:
  given tokensEncoder: Encoder[Tokens] = deriveEncoder[Tokens]
  given tokensEntityEncoder[F[_]: Concurrent]: EntityEncoder[F, Tokens] =
    jsonEncoderOf[F, Tokens]
