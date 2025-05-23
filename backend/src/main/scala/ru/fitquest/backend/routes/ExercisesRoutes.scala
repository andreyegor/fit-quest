package ru.fitquest.backend.routes

import cats.effect.Sync
import cats.syntax.all.*
import org.http4s.{AuthedRoutes, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher
import io.circe.Json

import ru.fitquest.backend.auth
import ru.fitquest.backend.exercises
import ru.fitquest.backend.core.structures.user.User
import org.http4s.QueryParamDecoder
import ru.fitquest.backend.core.structures.exercise.ExerciseType
import org.http4s.ParseFailure

object ExercisesRoutes:
  def GetExercises[F[_]: Sync](
      G: exercises.GetExercises[F]
  ): AuthedRoutes[User, F] =
    val dsl = new Http4sDsl[F] {}
    import dsl.*

    AuthedRoutes.of { case req @ GET -> Root / "exercises" as user =>
      val qp = req.req.uri.query.params

      val exerciseTypes: Either[String, Option[List[ExerciseType]]] =
        qp.get("types") match {
          case Some(s) =>
            s.split(",").toList.traverse(ExerciseType(_)).map(Some(_))
          case None => Right(None)
        }
      val from: Either[String, Option[Int]] = qp.get("offset") match {
        case Some(s) => s.toIntOption.toRight(s"Invalid offset: $s").map(Some(_))
        case None    => Right(None)
      }
      val to = qp.get("limit") match {
        case Some(s) => s.toIntOption.toRight(s"Invalid limit: $s").map(Some(_))
        case None    => Right(None)
      }

      (for {
        ets <- exerciseTypes
        f <- from
        t <- to
        diff = qp.keySet diff Set("types", "offset", "limit")
        _ <- if (diff.nonEmpty) Left(s"Unknown query parameters: ${diff.mkString(", ")}") else Right(())
      } yield (ets, f, t)) match {
        case Right(exerciseTypes, from, to) =>
          for {
            exercises <- G(user, exerciseTypes, from, to)
            resp <- Ok(exercises)
          } yield resp

        case Left(e) =>
          BadRequest(e)
      }
    }

  private object TypesMatcher
      extends OptionalQueryParamDecoderMatcher[List[ExerciseType]]("type")
  private object FromMatcher
      extends OptionalQueryParamDecoderMatcher[Int]("offset")
  private object ToMatcher
      extends OptionalQueryParamDecoderMatcher[Int]("limit")

  private given exerciseTypeListDecoder: QueryParamDecoder[List[ExerciseType]] =
    QueryParamDecoder.stringQueryParamDecoder.emap { s =>
      s.split(",").toList.traverse { part =>
        ExerciseType(part.trim)
          .leftMap(err => ParseFailure("ExerciseType", err))
      }
    }
