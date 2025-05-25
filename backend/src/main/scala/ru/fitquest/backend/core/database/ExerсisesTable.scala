package ru.fitquest.backend.core.database

import cats.effect.Sync
import cats.implicits.*
import cats.data.EitherT
import cats.data.OptionT
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*

import ru.fitquest.backend.core.structures.exercise.*
import ru.fitquest.backend.core.structures.user.*

trait ExercisesTable[F[_]](transactor: Transactor[F]) {
  def add(exercise: Exercise): F[Unit]
  def remove(exerciseId: ExerciseId): F[Unit]
  def get(
      userId: UserId,
      exerciseTypes: Option[List[ExerciseType]] = None,
      from: Option[Int] = None,
      to: Option[Int] = None
  ): F[List[Exercise]]
}

object ExercisesTable:
  def impl[F[_]: Sync](transactor: Transactor[F]): ExercisesTable[F] =
    new ExercisesTable[F](transactor):
      override def add(e: Exercise): F[Unit] =
        sql"""
        INSERT INTO exercises ( exercise_id, user_id, start_time, end_time, exercise_type, metrics, series)
        VALUES ( ${e.exerciseID.value}, 
                 ${e.userID.value}, 
                 ${e.startTime}, 
                 ${e.endTime}, 
                 ${e.exerciseType.value}, 
                 ${e.metrics}::jsonb, 
                 ${e.series}::jsonb)
        """.update.run
          .transact(transactor)
          .void

      override def remove(i: ExerciseId): F[Unit] =
        sql"""
        DELETE FROM exercises
        WHERE exercise_id = ${i.value}
        """.update.run
          .transact(transactor)
          .void

      def get(
          userId: UserId,
          exerciseTypes: Option[List[ExerciseType]] = None,
          from: Option[Int] = None,
          to: Option[Int] = None
      ): F[List[Exercise]] = {
        val filter: Fragment = exerciseTypes match {
          case Some(t) if t.nonEmpty =>
            val frs = t.map(t => fr"exercise_type = ${t.value}")
            fr"AND (" ++ frs.tail.foldLeft(frs.head) { (f, t) =>
              f ++ fr" OR " ++ t
            } ++ fr")"
          case _ => Fragment.empty
        }

        val pagination: Fragment = (from, to) match {
          case (Some(f), Some(t)) => fr"OFFSET $f LIMIT $t"
          case (Some(f), None)    => fr"OFFSET $f"
          case (None, Some(t))    => fr"LIMIT $t"
          case _                  => Fragment.empty
        }

        val query =
          fr"""
          SELECT exercise_id, user_id, exercise_type, start_time, end_time,  metrics, series
          FROM exercises
          WHERE user_id = ${userId.value}
          """ ++ filter ++ fr"ORDER BY start_time DESC" ++ pagination

        query.query[Exercise].to[List].transact(transactor)
      }
