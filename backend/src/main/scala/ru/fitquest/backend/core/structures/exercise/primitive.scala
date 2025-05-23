package ru.fitquest.backend.core.structures.exercise

import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}
import doobie.util.{Put, Get}
import doobie.postgres.implicits.*
import java.util.UUID
import io.circe.Encoder

opaque type ExerciseId = UUID
object ExerciseId:
  def apply(u: UUID): ExerciseId = u
  def random: ExerciseId = UUID.randomUUID()

  extension (u: ExerciseId) def value: UUID = u
  given Encoder[ExerciseId] = Encoder[UUID].contramap[ExerciseId](_.value)
  given Get[ExerciseId] = Get[UUID].map(ExerciseId(_))

opaque type ExerciseType = String
object ExerciseType:
  def apply(e: String): Either[String, ExerciseType] =
    if types contains e then Right(e)
    else Left(s"Invalid type: $e")
  extension (e: ExerciseType) def value: String = e
  given Conversion[ExerciseType, String] = _.value
  given Decoder[ExerciseType] = Decoder[String].emap(ExerciseType(_))
  given Encoder[ExerciseType] = Encoder[String].contramap[ExerciseType](_.value)
  given Get[ExerciseType] =
    Get[String].temap(ExerciseType(_).left.map(_.toString))

  private val types = Set("walking", "running", "cycling", "swimming")

opaque type Metrics = Map[String, Double]
object Metrics:
  def apply(m: Map[String, Double]): Either[String, Metrics] =
    if m.keySet.subsetOf(metrics) then Right(m)
    else
      Left(
        s"Invalid metrics: ${m.keySet.diff(metrics).mkString(", ")}"
      )
  extension (m: Metrics) def value: Map[String, Double] = m
  given Decoder[Metrics] = Decoder.decodeMap[String, Double].emap(Metrics(_))
  given Encoder[Metrics] =
    Encoder.encodeMap[String, Double].contramap[Metrics](_.value)
  given Get[Metrics] = Get[String].temap { s =>
    io.circe.parser
      .decode[Map[String, Double]](s)
      .left
      .map(_.getMessage)
      .flatMap(Metrics(_).left.map(_.toString))
  }
  given Put[Metrics] =
    Put[String].contramap[Metrics](m => m.value.asJson.noSpaces)

  private val metrics =
    Set("distanceMeters", "caloriesKcal", "steps")

opaque type Series = Map[String, List[Double]]
object Series:
  def apply(s: Map[String, List[Double]]): Either[String, Series] =
    if s.keySet.subsetOf(series) then Right(s)
    else
      Left(
        s"Invalid series: ${s.keySet.diff(series).mkString(", ")}"
      )
  extension (s: Series) def value: Map[String,List[Double]] = s
  given Decoder[Series] = Decoder.decodeMap[String, List[Double]].emap(Series(_))
  given Encoder[Series] =
    Encoder.encodeMap[String, List[Double]].contramap[Series](_.value)
  given Get[Series] = Get[String].temap { s =>
    io.circe.parser
      .decode[Map[String, List[Double]]](s)
      .left
      .map(_.getMessage)
      .flatMap(Series(_).left.map(_.toString))
  }
  given Put[Series] =
    Put[String].contramap[Series](s => s.value.asJson.noSpaces)

  private val series = Set("speedSeries", "heartRateSeries")
