package ru.fitquest.backend.core.structures.exercise

import java.time.Instant
import org.http4s.client.middleware.Metrics
import io.circe.Decoder
import io.circe.generic.semiauto._

case class NewExerciseRequest(
    exerciseType: ExerciseType,
    startTime: Instant,
    endTime: Instant,
    metrics: Metrics,
    series: Series
)

object NewExerciseRequest:
    implicit val newExerciseRequestDecoder: Decoder[NewExerciseRequest] = deriveDecoder
