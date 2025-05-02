package ru.fitquest.core.structures.user

import cats.effect.Concurrent
import io.circe.Encoder
import io.circe.generic.semiauto.*
import org.http4s.EntityEncoder
import org.http4s.circe.*

import ru.fitquest.core.types.*

case class UserResponse(
    id: UserId,
    name: Name,
    email: Email,
    googleId: Option[GoogleId]
)

object UserResponse {
  // Circe encoder to convert UserResponse to JSON
  import io.circe.Encoder
  import io.circe.generic.semiauto.deriveEncoder

  given tokensEncoder: Encoder[UserResponse] = deriveEncoder[UserResponse]
  given tokensEntityEncoder[F[_]: Concurrent]: EntityEncoder[F, UserResponse] =
    jsonEncoderOf[F, UserResponse]

  def from(u: User): UserResponse =
    UserResponse(
      id = u.userId,
      name = u.name,
      email = u.email,
      googleId = u.googleId
    )
}
