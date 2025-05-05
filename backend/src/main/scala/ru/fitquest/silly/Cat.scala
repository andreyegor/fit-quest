package ru.fitquest.silly

import cats.effect.Sync
import cats.implicits.*
import io.circe.syntax._
import io.circe.Json

import ru.fitquest.core.structures.user.User

trait Cat {
  def apply(user: User): Json
}

object Cat {
  final case class CatError(e: Throwable) extends RuntimeException
  def impl: Cat = new Cat {
    def apply(user: User): Json =
      val cat = """ /\_/\
                   ( o.o )
                    > ^ <"""
      val message = s"Кот специально для ${user.name}"

      Json.obj(
        "message" -> message.asJson,
        "cat" -> cat.asJson
      )
  }
}
