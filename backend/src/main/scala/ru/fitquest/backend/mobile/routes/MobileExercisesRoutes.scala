package ru.fitquest.backend.mobile.routes

import cats.data.EitherT
import cats.effect.Concurrent
import cats.implicits.*
import org.http4s.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.dsl.Http4sDsl
import org.http4s.AuthedRoutes
import org.typelevel.log4cats.Logger
import org.http4s.circe.jsonEncoder
import io.circe.syntax.*

import ru.fitquest.backend.core.structures.exercise.Exercise
import ru.fitquest.backend.auth.Login
import ru.fitquest.backend.core.structures.exercise.NewExerciseRequest
import ru.fitquest.backend.exercises.AddExercises
import ru.fitquest.backend.core.structures.user.User

object MobileExercisesRoutes {
  def AddExercises[F[_]: Concurrent](
      W: AddExercises[F]
  ): AuthedRoutes[User, F] =
    implicit val dsl = new Http4sDsl[F] {}
    import dsl.*

    AuthedRoutes.of {
      case req @ POST -> Root / "mobile" / "exercises" / "add" as user =>
        {
          for {
            exercises <- req.req.as[List[NewExerciseRequest]]
            _ <- W(user, exercises)
            resp <- Ok()
          } yield resp
        }.handleErrorWith { case e: org.http4s.InvalidMessageBodyFailure =>
          BadRequest("Invalid JSON body")
        }
    }
}
