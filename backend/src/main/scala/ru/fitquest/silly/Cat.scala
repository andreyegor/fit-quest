package ru.fitquest.silly

import cats.effect.Sync
import cats.implicits._
import io.circe.{Encoder, Json}

trait Cat[F[_]] {
  def cat: F[String]
}

object Cat {
  final case class CatError(e: Throwable) extends RuntimeException

  def impl[F[_] : Sync]: Cat[F] = new Cat[F] {
    def cat: F[String] =
        Sync[F].pure(
          """
            | /\_/\
            |( o.o )
            | > ^ <
            |""".stripMargin
        )
  }
}