package ru.fitquest.backend.core.structures.exercise

import java.time.Instant
import cats.instances.double
import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto._

import ru.fitquest.backend.core.structures.user.{User, UserId}

case class Exercise(
    exerciseID: ExerciseId,
    userID: UserId,
    exerciseType: ExerciseType,
    startTime: Instant,
    endTime: Instant,
    metrics: Metrics,
    series: Series
)

object Exercise:
  def create(e: NewExerciseRequest, u: User): Exercise =
    new Exercise(
      exerciseID = ExerciseId.random,
      userID = u.userId,
      exerciseType = e.exerciseType,
      startTime = e.startTime,
      endTime = e.endTime,
      metrics = e.metrics,
      series = e.series
    )

  implicit val exerciseEncoder: Encoder[Exercise] = deriveEncoder[Exercise]
