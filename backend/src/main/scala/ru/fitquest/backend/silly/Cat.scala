package ru.fitquest.backend.silly

import cats.effect.Sync
import cats.implicits.*
import io.circe.syntax._
import io.circe.Json

import ru.fitquest.backend.core.structures.user.User

trait Cat[F[_]] {
  def apply(user: User): F[Json]
}

object Cat:
  def impl[F[_]: Sync]: Cat[F] = new Cat[F]:
    def apply(user: User): F[Json] =
      val cat = "/\\_/\\\n( o.o )\n> ^ <"
      val message = s"Кот специально для ${user.name}"

      Json
        .obj(
          "message" -> message.asJson,
          "cat" -> cat.asJson
        )
        .pure[F]
