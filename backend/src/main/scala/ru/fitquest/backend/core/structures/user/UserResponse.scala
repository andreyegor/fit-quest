package ru.fitquest.backend.core.structures.user

import cats.effect.Concurrent
import io.circe.Encoder
import io.circe.generic.semiauto.*
import org.http4s.EntityEncoder
import org.http4s.circe.*

case class UserResponse(
    id: UserId,
    name: Name,
    email: Email
)

object UserResponse {
  given userResponseEncoder: Encoder[UserResponse] = deriveEncoder[UserResponse]
  given userResponseEntityEncoder[F[_]: Concurrent]
      : EntityEncoder[F, UserResponse] =
    jsonEncoderOf[F, UserResponse]

  def from(u: User): UserResponse =
    UserResponse(
      id = u.userId,
      name = u.name,
      email = u.email,
    )
}
